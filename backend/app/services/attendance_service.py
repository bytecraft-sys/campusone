import uuid
from datetime import date, datetime, timedelta, timezone
from typing import List, Optional, Tuple
from sqlalchemy import select, func, and_
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.exceptions import (
    CampusOneException,
    ConflictException,
    ForbiddenException,
    NotFoundException,
)
from app.models.enums import (
    AttendanceStatus,
    CorrectionStatus,
    EnrollmentStatus,
    SessionStatus,
    UserRole,
)
from app.models.academic import (
    Enrollment,
    Section,
    StudentProfile,
    Subject,
    TeacherProfile,
    TeachingAssignment,
    TeacherSubstitution,
)
from app.models.attendance import (
    AttendanceCorrection,
    AttendanceRecord,
    AttendanceSession,
)
from app.models.identity import User
from app.models.platform import AuditLog, PolicySetting
from app.schemas.attendance import (
    CorrectionRequest,
    CorrectionResponse,
    FinalizeSessionResponse,
    OpenSessionRequest,
    SaveRecordsRequest,
    SaveRecordsResponse,
    StudentAttendanceSummaryResponse,
    SubjectAttendanceSummary,
)


class AttendanceService:
    def __init__(self, db: AsyncSession):
        self.db = db

    @staticmethod
    def calculate_attendance_percentage(
        present_count: int,
        absent_count: int,
        threshold: float = 75.0
    ) -> Tuple[Optional[float], bool, str]:
        counted = present_count + absent_count
        if counted == 0:
            return None, False, "No attendance data"

        percentage = round((present_count / counted) * 100, 2)
        is_low = percentage < threshold
        display_text = f"{percentage}%"
        return percentage, is_low, display_text

    async def get_teacher_profile(self, user: User) -> TeacherProfile:
        result = await self.db.execute(
            select(TeacherProfile).where(TeacherProfile.user_id == user.id)
        )
        teacher = result.scalar_one_or_none()
        if not teacher:
            raise ForbiddenException(code="not_teacher", message="User does not have a teacher profile.")
        return teacher

    async def get_student_profile(self, user: User) -> StudentProfile:
        result = await self.db.execute(
            select(StudentProfile).where(StudentProfile.user_id == user.id)
        )
        student = result.scalar_one_or_none()
        if not student:
            raise ForbiddenException(code="not_student", message="User does not have a student profile.")
        return student

    async def verify_teacher_allocation(
        self, teacher_id: uuid.UUID, section_id: uuid.UUID, subject_id: Optional[uuid.UUID], session_date: date
    ):
        """Verifies if the teacher is assigned or substituted for this section and subject."""
        # 1. Check primary teaching assignment
        query = select(TeachingAssignment).where(
            TeachingAssignment.teacher_id == teacher_id,
            TeachingAssignment.section_id == section_id,
        )
        if subject_id:
            query = query.where(TeachingAssignment.subject_id == subject_id)
        
        assign_result = await self.db.execute(query)
        if assign_result.scalar_one_or_none():
            return True

        # 2. Check active substitution
        sub_query = select(TeacherSubstitution).where(
            TeacherSubstitution.substitute_teacher_id == teacher_id,
            TeacherSubstitution.section_id == section_id,
            TeacherSubstitution.from_date <= session_date,
            TeacherSubstitution.to_date >= session_date,
        )
        if subject_id:
            sub_query = sub_query.where(TeacherSubstitution.subject_id == subject_id)

        sub_result = await self.db.execute(sub_query)
        if sub_result.scalar_one_or_none():
            return True

        raise ForbiddenException(
            code="not_allocated",
            message="You do not hold an active allocation or substitution for this section and subject."
        )

    async def open_session(self, user: User, payload: OpenSessionRequest) -> AttendanceSession:
        teacher = await self.get_teacher_profile(user)
        await self.verify_teacher_allocation(
            teacher.id, payload.section_id, payload.subject_id, payload.session_date
        )

        # Check duplicate session
        dup_query = select(AttendanceSession).where(
            AttendanceSession.section_id == payload.section_id,
            AttendanceSession.subject_id == payload.subject_id,
            AttendanceSession.session_date == payload.session_date,
            AttendanceSession.sequence_no == payload.sequence_no,
        )
        existing = await self.db.execute(dup_query)
        if existing.scalar_one_or_none():
            raise ConflictException(
                code="session_exists",
                message="An attendance session already exists for this section, subject, date, and sequence number."
            )

        # Create session
        session = AttendanceSession(
            section_id=payload.section_id,
            subject_id=payload.subject_id,
            teacher_id=teacher.id,
            session_date=payload.session_date,
            sequence_no=payload.sequence_no,
            status=SessionStatus.OPEN,
        )
        self.db.add(session)
        await self.db.flush()

        # Find all active enrolled students in this section
        enrollments_res = await self.db.execute(
            select(Enrollment).where(
                Enrollment.section_id == payload.section_id,
                Enrollment.status == EnrollmentStatus.ACTIVE,
            )
        )
        enrollments = enrollments_res.scalars().all()

        for enr in enrollments:
            record = AttendanceRecord(
                session_id=session.id,
                student_id=enr.student_id,
                status=AttendanceStatus.NOT_MARKED,
            )
            self.db.add(record)

        await self.db.commit()

        # Reload with records
        res = await self.db.execute(
            select(AttendanceSession)
            .where(AttendanceSession.id == session.id)
            .options(selectinload(AttendanceSession.records))
        )
        return res.scalar_one()

    async def save_records(
        self, user: User, session_id: uuid.UUID, payload: SaveRecordsRequest
    ) -> SaveRecordsResponse:
        teacher = await self.get_teacher_profile(user)
        sess_res = await self.db.execute(
            select(AttendanceSession).where(AttendanceSession.id == session_id)
        )
        session = sess_res.scalar_one_or_none()
        if not session:
            raise NotFoundException(code="session_not_found", message="Attendance session not found.")

        if session.status == SessionStatus.FINALIZED:
            raise ConflictException(
                code="session_already_finalized",
                message="Cannot update records in a finalized attendance session. Use correction workflow."
            )

        await self.verify_teacher_allocation(
            teacher.id, session.section_id, session.subject_id, session.session_date
        )

        # Get existing records
        rec_res = await self.db.execute(
            select(AttendanceRecord).where(AttendanceRecord.session_id == session_id)
        )
        records_map = {r.student_id: r for r in rec_res.scalars().all()}

        saved_count = 0
        for item in payload.records:
            if item.student_id not in records_map:
                raise CampusOneException(
                    code="student_not_enrolled",
                    message=f"Student {item.student_id} is not enrolled in this section.",
                    status_code=400
                )
            record = records_map[item.student_id]
            record.status = item.status
            saved_count += 1

        await self.db.commit()

        # Count how many remain NOT_MARKED
        nm_count = sum(1 for r in records_map.values() if r.status == AttendanceStatus.NOT_MARKED)
        return SaveRecordsResponse(saved=saved_count, not_marked=nm_count)

    async def finalize_session(self, user: User, session_id: uuid.UUID) -> FinalizeSessionResponse:
        teacher = await self.get_teacher_profile(user)
        sess_res = await self.db.execute(
            select(AttendanceSession).where(AttendanceSession.id == session_id)
        )
        session = sess_res.scalar_one_or_none()
        if not session:
            raise NotFoundException(code="session_not_found", message="Attendance session not found.")

        await self.verify_teacher_allocation(
            teacher.id, session.section_id, session.subject_id, session.session_date
        )

        now = datetime.now(timezone.utc)
        session.status = SessionStatus.FINALIZED
        session.finalized_at = now
        await self.db.commit()

        return FinalizeSessionResponse(status=SessionStatus.FINALIZED, finalized_at=now)

    async def correct_record(
        self, user: User, record_id: uuid.UUID, payload: CorrectionRequest
    ) -> CorrectionResponse:
        teacher = await self.get_teacher_profile(user)

        rec_res = await self.db.execute(
            select(AttendanceRecord)
            .where(AttendanceRecord.id == record_id)
            .options(selectinload(AttendanceRecord.session))
        )
        record = rec_res.scalar_one_or_none()
        if not record:
            raise NotFoundException(code="record_not_found", message="Attendance record not found.")

        session = record.session
        await self.verify_teacher_allocation(
            teacher.id, session.section_id, session.subject_id, session.session_date
        )

        # Check policy for correction window (default 7 days)
        pol_res = await self.db.execute(
            select(PolicySetting).where(PolicySetting.key == "attendance_correction_window_days")
        )
        pol = pol_res.scalar_one_or_none()
        window_days = int(pol.value) if pol and pol.value.isdigit() else 7

        days_passed = (date.today() - session.session_date).days
        requires_admin = days_passed > window_days

        corr_status = CorrectionStatus.PENDING if requires_admin else CorrectionStatus.APPROVED

        correction = AttendanceCorrection(
            record_id=record.id,
            teacher_id=teacher.id,
            previous_status=record.status,
            new_status=payload.new_status,
            reason=payload.reason,
            requires_admin_approval=requires_admin,
            status=corr_status,
        )
        self.db.add(correction)

        if not requires_admin:
            # Immediately apply the correction
            record.status = payload.new_status

        # Append audit log
        audit = AuditLog(
            actor_id=user.id,
            action="attendance_correction",
            resource_type="attendance_record",
            resource_id=str(record.id),
            old_value=str(record.status),
            new_value=str(payload.new_status),
        )
        self.db.add(audit)

        await self.db.commit()

        return CorrectionResponse(
            id=correction.id,
            requires_admin_approval=requires_admin,
            status=corr_status,
        )

    async def get_student_summary(self, user: User) -> StudentAttendanceSummaryResponse:
        student = await self.get_student_profile(user)

        # Query all records for this student in FINALIZED sessions
        query = (
            select(AttendanceRecord, AttendanceSession, Subject)
            .join(AttendanceSession, AttendanceRecord.session_id == AttendanceSession.id)
            .outerjoin(Subject, AttendanceSession.subject_id == Subject.id)
            .where(
                AttendanceRecord.student_id == student.id,
                AttendanceSession.status == SessionStatus.FINALIZED,
            )
        )
        results = (await self.db.execute(query)).all()

        total_present = 0
        total_absent = 0
        subjects_data = {}

        for rec, sess, subj in results:
            if rec.status == AttendanceStatus.NOT_MARKED:
                continue

            is_p = rec.status == AttendanceStatus.PRESENT
            if is_p:
                total_present += 1
            else:
                total_absent += 1

            subj_id = sess.subject_id
            subj_name = subj.name if subj else "Daily Attendance"

            if subj_id not in subjects_data:
                subjects_data[subj_id] = {
                    "subject_id": subj_id,
                    "subject_name": subj_name,
                    "present_count": 0,
                    "absent_count": 0,
                }
            if is_p:
                subjects_data[subj_id]["present_count"] += 1
            else:
                subjects_data[subj_id]["absent_count"] += 1

        overall_pct, is_low, display = self.calculate_attendance_percentage(
            total_present, total_absent
        )

        subject_summaries = []
        for s_info in subjects_data.values():
            s_pct, s_low, _ = self.calculate_attendance_percentage(
                s_info["present_count"], s_info["absent_count"]
            )
            subject_summaries.append(
                SubjectAttendanceSummary(
                    subject_id=s_info["subject_id"],
                    subject_name=s_info["subject_name"],
                    present_count=s_info["present_count"],
                    absent_count=s_info["absent_count"],
                    percentage=s_pct,
                    is_low=s_low,
                )
            )

        return StudentAttendanceSummaryResponse(
            overall_percentage=overall_pct,
            is_low=is_low,
            display_text=display,
            total_present=total_present,
            total_absent=total_absent,
            subjects=subject_summaries,
        )
