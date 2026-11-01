import uuid
from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy import select, and_, update
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.exceptions import (
    CampusOneException,
    ConflictException,
    ForbiddenException,
    NotFoundException,
)
from app.models.enums import AssignmentStatus, EnrollmentStatus, SubmissionStatus, UserRole
from app.models.academic import (
    Enrollment,
    StudentProfile,
    TeacherProfile,
    TeachingAssignment,
)
from app.models.assignments import (
    Assignment,
    AssignmentAttachment,
    AssignmentExtension,
    Submission,
    SubmissionFile,
)
from app.models.identity import User
from app.models.platform import AuditLog
from app.schemas.assignments import (
    AssignmentCreate,
    GradeSubmissionRequest,
    GrantExtensionRequest,
    SubmissionCreate,
)


class AssignmentService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_teacher_profile(self, user: User) -> TeacherProfile:
        res = await self.db.execute(select(TeacherProfile).where(TeacherProfile.user_id == user.id))
        teacher = res.scalar_one_or_none()
        if not teacher:
            raise ForbiddenException(code="not_teacher", message="User does not have a teacher profile.")
        return teacher

    async def get_student_profile(self, user: User) -> StudentProfile:
        res = await self.db.execute(select(StudentProfile).where(StudentProfile.user_id == user.id))
        student = res.scalar_one_or_none()
        if not student:
            raise ForbiddenException(code="not_student", message="User does not have a student profile.")
        return student

    async def verify_teacher_allocation(self, teacher_id: uuid.UUID, section_id: uuid.UUID, subject_id: uuid.UUID):
        query = select(TeachingAssignment).where(
            TeachingAssignment.teacher_id == teacher_id,
            TeachingAssignment.section_id == section_id,
            TeachingAssignment.subject_id == subject_id,
        )
        res = await self.db.execute(query)
        if not res.scalar_one_or_none():
            raise ForbiddenException(
                code="not_allocated",
                message="Teacher does not hold an active allocation for this section and subject."
            )

    async def create_assignment(self, user: User, payload: AssignmentCreate) -> Assignment:
        teacher = await self.get_teacher_profile(user)
        await self.verify_teacher_allocation(teacher.id, payload.section_id, payload.subject_id)

        assignment = Assignment(
            section_id=payload.section_id,
            subject_id=payload.subject_id,
            teacher_id=teacher.id,
            title=payload.title,
            instructions=payload.instructions,
            due_at=payload.due_at,
            max_marks=payload.max_marks,
            allow_late=payload.allow_late,
            late_until=payload.late_until,
            late_penalty_percent=payload.late_penalty_percent,
            allow_resubmission=payload.allow_resubmission,
            status=payload.status,
        )
        self.db.add(assignment)
        await self.db.flush()

        for fid in payload.attachment_file_ids:
            self.db.add(AssignmentAttachment(assignment_id=assignment.id, file_id=fid))

        await self.db.commit()
        await self.db.refresh(assignment)
        return assignment

    async def list_assignments(self, user: User) -> List[Assignment]:
        if user.role == UserRole.STUDENT:
            student = await self.get_student_profile(user)
            # Find active sections
            enr_res = await self.db.execute(
                select(Enrollment.section_id).where(
                    Enrollment.student_id == student.id,
                    Enrollment.status == EnrollmentStatus.ACTIVE,
                )
            )
            section_ids = enr_res.scalars().all()
            # Students see only PUBLISHED assignments
            query = select(Assignment).where(
                Assignment.section_id.in_(section_ids),
                Assignment.status == AssignmentStatus.PUBLISHED,
            ).order_by(Assignment.due_at.asc())
            return (await self.db.execute(query)).scalars().all()

        elif user.role == UserRole.TEACHER:
            teacher = await self.get_teacher_profile(user)
            query = select(Assignment).where(
                Assignment.teacher_id == teacher.id
            ).order_by(Assignment.due_at.desc())
            return (await self.db.execute(query)).scalars().all()

        else:
            # Admin sees all
            query = select(Assignment).order_by(Assignment.due_at.desc())
            return (await self.db.execute(query)).scalars().all()

    async def submit_assignment(
        self, user: User, assignment_id: uuid.UUID, payload: SubmissionCreate
    ) -> Submission:
        student = await self.get_student_profile(user)

        res = await self.db.execute(select(Assignment).where(Assignment.id == assignment_id))
        assignment = res.scalar_one_or_none()
        if not assignment:
            raise NotFoundException(code="assignment_not_found", message="Assignment not found.")

        if assignment.status != AssignmentStatus.PUBLISHED:
            raise CampusOneException(
                code="assignment_not_active",
                message="Submissions are not accepted for assignments in draft or closed state.",
                status_code=400,
            )

        # Check student enrollment in this section
        enr_res = await self.db.execute(
            select(Enrollment).where(
                Enrollment.student_id == student.id,
                Enrollment.section_id == assignment.section_id,
                Enrollment.status == EnrollmentStatus.ACTIVE,
            )
        )
        if not enr_res.scalar_one_or_none():
            raise ForbiddenException(
                code="not_enrolled",
                message="You are not enrolled in the section for this assignment."
            )

        now = datetime.now(timezone.utc)

        # Check if student has an individual extension
        ext_res = await self.db.execute(
            select(AssignmentExtension).where(
                AssignmentExtension.assignment_id == assignment.id,
                AssignmentExtension.student_id == student.id,
            )
        )
        extension = ext_res.scalar_one_or_none()
        effective_due_at = extension.extended_due_at if extension else assignment.due_at
        if effective_due_at.tzinfo is None:
            effective_due_at = effective_due_at.replace(tzinfo=timezone.utc)

        late_until = assignment.late_until
        if late_until and late_until.tzinfo is None:
            late_until = late_until.replace(tzinfo=timezone.utc)

        # Check deadlines and late submission rules
        is_late = False
        if now > effective_due_at:
            if not assignment.allow_late:
                raise CampusOneException(
                    code="submission_closed",
                    message="The deadline has passed and late submissions are not permitted.",
                    status_code=400,
                )
            if late_until and now > late_until:
                raise CampusOneException(
                    code="late_deadline_passed",
                    message="The late submission window has closed.",
                    status_code=400,
                )
            is_late = True


        # Check existing submissions for resubmission policy & versioning
        existing_subs_res = await self.db.execute(
            select(Submission).where(
                Submission.assignment_id == assignment.id,
                Submission.student_id == student.id,
            )
        )
        existing_subs = existing_subs_res.scalars().all()

        if existing_subs and not assignment.allow_resubmission:
            raise ConflictException(
                code="resubmission_disallowed",
                message="Resubmissions are not permitted for this assignment."
            )

        # Mark all previous versions as not current
        for s in existing_subs:
            s.is_current = False
        await self.db.flush()

        new_version_no = len(existing_subs) + 1


        submission = Submission(
            assignment_id=assignment.id,
            student_id=student.id,
            version_no=new_version_no,
            is_current=True,
            text_answer=payload.text_answer,
            status=SubmissionStatus.SUBMITTED,
            submitted_at=now,
            is_late=is_late,
        )
        self.db.add(submission)
        await self.db.flush()

        for fid in payload.file_ids:
            self.db.add(SubmissionFile(submission_id=submission.id, file_id=fid))

        await self.db.commit()
        await self.db.refresh(submission)
        return submission

    async def grant_extension(
        self, user: User, assignment_id: uuid.UUID, payload: GrantExtensionRequest
    ) -> AssignmentExtension:
        teacher = await self.get_teacher_profile(user)
        assign_res = await self.db.execute(select(Assignment).where(Assignment.id == assignment_id))
        assignment = assign_res.scalar_one_or_none()
        if not assignment:
            raise NotFoundException(code="assignment_not_found", message="Assignment not found.")

        await self.verify_teacher_allocation(teacher.id, assignment.section_id, assignment.subject_id)

        # Upsert extension
        ext_res = await self.db.execute(
            select(AssignmentExtension).where(
                AssignmentExtension.assignment_id == assignment.id,
                AssignmentExtension.student_id == payload.student_id,
            )
        )
        extension = ext_res.scalar_one_or_none()
        if extension:
            extension.extended_due_at = payload.extended_due_at
            extension.reason = payload.reason
        else:
            extension = AssignmentExtension(
                assignment_id=assignment.id,
                student_id=payload.student_id,
                extended_due_at=payload.extended_due_at,
                reason=payload.reason,
            )
            self.db.add(extension)

        await self.db.commit()
        await self.db.refresh(extension)
        return extension

    async def grade_submission(
        self, user: User, submission_id: uuid.UUID, payload: GradeSubmissionRequest
    ) -> Submission:
        teacher = await self.get_teacher_profile(user)

        res = await self.db.execute(
            select(Submission)
            .where(Submission.id == submission_id)
            .options(selectinload(Submission.assignment))
        )
        submission = res.scalar_one_or_none()
        if not submission:
            raise NotFoundException(code="submission_not_found", message="Submission not found.")

        assignment = submission.assignment
        await self.verify_teacher_allocation(teacher.id, assignment.section_id, assignment.subject_id)

        if payload.marks < 0 or payload.marks > assignment.max_marks:
            raise CampusOneException(
                code="validation_error",
                message=f"Marks must be between 0 and {assignment.max_marks}.",
                status_code=400,
            )

        old_marks = submission.marks
        submission.marks = payload.marks
        submission.feedback = payload.feedback
        submission.status = payload.status

        # Append audit log
        self.db.add(AuditLog(
            actor_id=user.id,
            action="grade_submission",
            resource_type="submission",
            resource_id=str(submission.id),
            old_value=str(old_marks),
            new_value=str(payload.marks),
        ))

        await self.db.commit()
        await self.db.refresh(submission)
        return submission
