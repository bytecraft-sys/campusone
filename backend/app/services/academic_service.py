import csv
import io
import uuid
from datetime import date, datetime, timezone
from typing import List, Optional
from sqlalchemy import select, and_, or_, update
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.exceptions import (
    CampusOneException,
    ConflictException,
    ForbiddenException,
    NotFoundException,
)
from app.core.security import generate_activation_token, get_password_hash
from app.models.enums import EnrollmentStatus, UserRole
from app.models.identity import ActivationToken, User
from app.models.academic import (
    AcademicYear,
    Course,
    CourseSubject,
    Department,
    Enrollment,
    Section,
    StudentProfile,
    Subject,
    TeacherProfile,
    TeachingAssignment,
    TeacherSubstitution,
    TimeSlot,
    TimetableEntry,
)
from app.models.platform import AuditLog
from app.schemas.academic import (
    AcademicYearCreate,
    AdminUserCreate,
    AdminUserCreateResponse,
    CourseCreate,
    CourseSubjectCreate,
    CSVImportDryRunReport,
    CSVRowValidation,
    DepartmentCreate,
    EnrollmentCreate,
    SectionCreate,
    SubjectCreate,
    TeacherSubstitutionCreate,
    TeachingAssignmentCreate,
    TimeSlotCreate,
    TimetableEntryCreate,
    TodayScheduleItem,
)


class AcademicService:
    def __init__(self, db: AsyncSession):
        self.db = db

    # --- Academic Years ---
    async def create_academic_year(self, payload: AcademicYearCreate) -> AcademicYear:
        if payload.is_current:
            # Set all other academic years to is_current=False
            await self.db.execute(update(AcademicYear).values(is_current=False))

        ay = AcademicYear(
            name=payload.name,
            start_date=payload.start_date,
            end_date=payload.end_date,
            is_current=payload.is_current,
        )
        self.db.add(ay)
        await self.db.commit()
        await self.db.refresh(ay)
        return ay

    async def list_academic_years(self) -> List[AcademicYear]:
        res = await self.db.execute(select(AcademicYear).order_by(AcademicYear.start_date.desc()))
        return res.scalars().all()

    # --- Departments ---
    async def create_department(self, payload: DepartmentCreate) -> Department:
        existing = await self.db.execute(select(Department).where(Department.code == payload.code))
        if existing.scalar_one_or_none():
            raise ConflictException(code="department_exists", message=f"Department with code '{payload.code}' already exists.")

        dept = Department(code=payload.code, name=payload.name)
        self.db.add(dept)
        await self.db.commit()
        await self.db.refresh(dept)
        return dept

    async def list_departments(self) -> List[Department]:
        res = await self.db.execute(select(Department).order_by(Department.name.asc()))
        return res.scalars().all()

    # --- Courses ---
    async def create_course(self, payload: CourseCreate) -> Course:
        existing = await self.db.execute(select(Course).where(Course.code == payload.code))
        if existing.scalar_one_or_none():
            raise ConflictException(code="course_exists", message=f"Course with code '{payload.code}' already exists.")

        course = Course(
            department_id=payload.department_id,
            code=payload.code,
            name=payload.name,
            total_semesters=payload.total_semesters,
        )
        self.db.add(course)
        await self.db.commit()
        await self.db.refresh(course)
        return course

    async def list_courses(self) -> List[Course]:
        res = await self.db.execute(select(Course).order_by(Course.code.asc()))
        return res.scalars().all()

    # --- Subjects ---
    async def create_subject(self, payload: SubjectCreate) -> Subject:
        existing = await self.db.execute(select(Subject).where(Subject.code == payload.code))
        if existing.scalar_one_or_none():
            raise ConflictException(code="subject_exists", message=f"Subject with code '{payload.code}' already exists.")

        subj = Subject(
            code=payload.code,
            name=payload.name,
            credit_hours=payload.credit_hours,
        )
        self.db.add(subj)
        await self.db.commit()
        await self.db.refresh(subj)
        return subj

    async def list_subjects(self) -> List[Subject]:
        res = await self.db.execute(select(Subject).order_by(Subject.code.asc()))
        return res.scalars().all()

    # --- Course Subjects ---
    async def map_course_subject(self, payload: CourseSubjectCreate) -> CourseSubject:
        cs = CourseSubject(
            course_id=payload.course_id,
            subject_id=payload.subject_id,
            semester_no=payload.semester_no,
            is_elective=payload.is_elective,
        )
        self.db.add(cs)
        await self.db.commit()
        await self.db.refresh(cs)
        return cs

    async def list_course_subjects(self, course_id: Optional[uuid.UUID] = None) -> List[CourseSubject]:
        query = select(CourseSubject)
        if course_id:
            query = query.where(CourseSubject.course_id == course_id)
        res = await self.db.execute(query)
        return res.scalars().all()

    # --- Sections ---
    async def create_section(self, payload: SectionCreate) -> Section:
        existing = await self.db.execute(
            select(Section).where(
                Section.course_id == payload.course_id,
                Section.academic_year_id == payload.academic_year_id,
                Section.semester_no == payload.semester_no,
                Section.name == payload.name,
            )
        )
        if existing.scalar_one_or_none():
            raise ConflictException(code="section_exists", message="Section already exists for this course, semester, and academic year.")

        section = Section(
            course_id=payload.course_id,
            academic_year_id=payload.academic_year_id,
            semester_no=payload.semester_no,
            name=payload.name,
        )
        self.db.add(section)
        await self.db.commit()
        await self.db.refresh(section)
        return section

    async def list_sections(self, course_id: Optional[uuid.UUID] = None) -> List[Section]:
        query = select(Section)
        if course_id:
            query = query.where(Section.course_id == course_id)
        res = await self.db.execute(query)
        return res.scalars().all()

    # --- Teaching Allocations & Timetable ---
    async def create_teaching_assignment(self, payload: TeachingAssignmentCreate) -> TeachingAssignment:
        existing = await self.db.execute(
            select(TeachingAssignment).where(
                TeachingAssignment.section_id == payload.section_id,
                TeachingAssignment.subject_id == payload.subject_id,
                TeachingAssignment.academic_year_id == payload.academic_year_id,
            )
        )
        if existing.scalar_one_or_none():
            raise ConflictException(
                code="allocation_conflict",
                message="A teaching assignment already exists for this section and subject in this academic year."
            )

        ta = TeachingAssignment(
            teacher_id=payload.teacher_id,
            section_id=payload.section_id,
            subject_id=payload.subject_id,
            academic_year_id=payload.academic_year_id,
        )
        self.db.add(ta)
        await self.db.commit()
        await self.db.refresh(ta)
        return ta

    async def list_teaching_assignments(self, section_id: Optional[uuid.UUID] = None) -> List[TeachingAssignment]:
        query = select(TeachingAssignment)
        if section_id:
            query = query.where(TeachingAssignment.section_id == section_id)
        res = await self.db.execute(query)
        return res.scalars().all()

    async def create_time_slot(self, payload: TimeSlotCreate) -> TimeSlot:
        slot = TimeSlot(
            day_of_week=payload.day_of_week,
            start_time=payload.start_time,
            end_time=payload.end_time,
            label=payload.label,
        )
        self.db.add(slot)
        await self.db.commit()
        await self.db.refresh(slot)
        return slot

    async def create_timetable_entry(self, payload: TimetableEntryCreate) -> TimetableEntry:
        # Check teacher schedule conflict (Page 15 in UI Skeleton)
        teacher_conflict = await self.db.execute(
            select(TimetableEntry).where(
                TimetableEntry.teacher_id == payload.teacher_id,
                TimetableEntry.time_slot_id == payload.time_slot_id,
            )
        )
        if teacher_conflict.scalar_one_or_none():
            raise ConflictException(
                code="teacher_schedule_conflict",
                message="This teacher already has a class scheduled in this time slot."
            )

        # Check section schedule conflict
        section_conflict = await self.db.execute(
            select(TimetableEntry).where(
                TimetableEntry.section_id == payload.section_id,
                TimetableEntry.time_slot_id == payload.time_slot_id,
            )
        )
        if section_conflict.scalar_one_or_none():
            raise ConflictException(
                code="section_schedule_conflict",
                message="This section already has a class scheduled in this time slot."
            )

        entry = TimetableEntry(
            section_id=payload.section_id,
            subject_id=payload.subject_id,
            teacher_id=payload.teacher_id,
            time_slot_id=payload.time_slot_id,
        )
        self.db.add(entry)
        await self.db.commit()
        await self.db.refresh(entry)
        return entry

    async def get_today_schedule(self, user: User) -> List[TodayScheduleItem]:
        # Monday is 0, Sunday is 6
        today_weekday = date.today().weekday()

        query = (
            select(TimetableEntry, Subject, Section, TeacherProfile, User, TimeSlot)
            .join(Subject, TimetableEntry.subject_id == Subject.id)
            .join(Section, TimetableEntry.section_id == Section.id)
            .join(TeacherProfile, TimetableEntry.teacher_id == TeacherProfile.id)
            .join(User, TeacherProfile.user_id == User.id)
            .join(TimeSlot, TimetableEntry.time_slot_id == TimeSlot.id)
            .where(TimeSlot.day_of_week == today_weekday)
        )

        if user.role == UserRole.TEACHER:
            teacher_res = await self.db.execute(select(TeacherProfile).where(TeacherProfile.user_id == user.id))
            teacher = teacher_res.scalar_one_or_none()
            if not teacher:
                return []
            query = query.where(TimetableEntry.teacher_id == teacher.id)

        elif user.role == UserRole.STUDENT:
            student_res = await self.db.execute(select(StudentProfile).where(StudentProfile.user_id == user.id))
            student = student_res.scalar_one_or_none()
            if not student:
                return []
            # Find student's active section
            enr_res = await self.db.execute(
                select(Enrollment.section_id).where(
                    Enrollment.student_id == student.id,
                    Enrollment.status == EnrollmentStatus.ACTIVE,
                )
            )
            section_ids = enr_res.scalars().all()
            query = query.where(TimetableEntry.section_id.in_(section_ids))

        query = query.order_by(TimeSlot.start_time.asc())
        results = (await self.db.execute(query)).all()

        items = []
        for entry, subj, sec, t_prof, u_teacher, slot in results:
            items.append(TodayScheduleItem(
                id=entry.id,
                subject_name=subj.name,
                subject_code=subj.code,
                teacher_name=u_teacher.full_name,
                section_name=sec.name,
                start_time=slot.start_time,
                end_time=slot.end_time,
                period_label=slot.label,
            ))
        return items

    # --- Enrollments ---
    async def enroll_student(self, payload: EnrollmentCreate) -> Enrollment:
        # Section 9.2: Exactly one ACTIVE enrollment per student per academic year
        existing_active = await self.db.execute(
            select(Enrollment).where(
                Enrollment.student_id == payload.student_id,
                Enrollment.academic_year_id == payload.academic_year_id,
                Enrollment.status == EnrollmentStatus.ACTIVE,
            )
        )
        if existing_active.scalar_one_or_none():
            raise ConflictException(
                code="duplicate_active_enrollment",
                message="Student already has an active enrollment in this academic year."
            )

        enr = Enrollment(
            student_id=payload.student_id,
            section_id=payload.section_id,
            academic_year_id=payload.academic_year_id,
            status=EnrollmentStatus.ACTIVE,
        )
        self.db.add(enr)
        await self.db.commit()
        await self.db.refresh(enr)
        return enr

    # --- User Provisioning via Admin ---
    async def create_user_by_admin(self, admin_user: User, payload: AdminUserCreate) -> AdminUserCreateResponse:
        existing = await self.db.execute(
            select(User).where(or_(User.login_id == payload.login_id, User.email == payload.email))
        )
        if existing.scalar_one_or_none():
            raise ConflictException(code="user_exists", message="A user with this login ID or email already exists.")

        # Section 3.3 & 10.2: No password is created. Temporary placeholder hashed.
        placeholder_hash = get_password_hash(uuid.uuid4().hex)
        user = User(
            login_id=payload.login_id,
            email=payload.email,
            full_name=payload.full_name,
            hashed_password=placeholder_hash,
            role=payload.role,
            is_active=False,
            must_change_password=True,
        )
        self.db.add(user)
        await self.db.flush()

        if payload.role == UserRole.TEACHER:
            dept_id = payload.department_id
            if not dept_id:
                first_dept = (await self.db.execute(select(Department))).scalars().first()
                dept_id = first_dept.id if first_dept else None
            if dept_id:
                self.db.add(TeacherProfile(
                    user_id=user.id,
                    employee_code=payload.login_id,
                    department_id=dept_id,
                ))

        elif payload.role == UserRole.STUDENT:
            student_prof = StudentProfile(
                user_id=user.id,
                admission_number=payload.login_id,
                date_of_birth=date(2005, 1, 1),
            )
            self.db.add(student_prof)
            await self.db.flush()

            if payload.section_id:
                current_ay = (await self.db.execute(select(AcademicYear).where(AcademicYear.is_current == True))).scalar_one_or_none()
                if current_ay:
                    self.db.add(Enrollment(
                        student_id=student_prof.id,
                        section_id=payload.section_id,
                        academic_year_id=current_ay.id,
                        status=EnrollmentStatus.ACTIVE,
                    ))

        # Generate single-use 72-hour activation token
        raw_token, hashed_token, expires_at = generate_activation_token()
        self.db.add(ActivationToken(
            user_id=user.id,
            hashed_token=hashed_token,
            expires_at=expires_at,
            used=False,
        ))

        # Audit event
        self.db.add(AuditLog(
            actor_id=admin_user.id,
            action="user.create",
            resource_type="user",
            resource_id=str(user.id),
            new_value=f"Created {payload.role.value}: {payload.login_id}",
        ))

        await self.db.commit()

        activation_link = f"/activate?token={raw_token}"
        return AdminUserCreateResponse(
            user_id=user.id,
            login_id=user.login_id,
            role=user.role,
            activation_token=raw_token,
            activation_link=activation_link,
            expires_at=expires_at.isoformat(),
        )

    async def list_users(self) -> List[User]:
        result = await self.db.execute(select(User).order_by(User.created_at.desc()))
        return list(result.scalars().all())

    async def dry_run_csv_import(self, csv_content: str) -> CSVImportDryRunReport:
        """Section 3.3: CSV import dry-run validation report before confirming."""
        reader = csv.DictReader(io.StringIO(csv_content))
        validations = []
        seen_ids = set()
        seen_emails = set()

        for idx, row in enumerate(reader, start=2):
            name = row.get("full_name", "").strip()
            login_id = row.get("login_id", "").strip()
            email = row.get("email", "").strip()
            role = row.get("role", "").strip().upper()

            error = None
            if not name or not login_id or not email:
                error = "Missing required fields"
            elif role not in [UserRole.STUDENT.value, UserRole.TEACHER.value, UserRole.ADMIN.value]:
                error = f"Invalid role '{role}'"
            elif login_id in seen_ids:
                error = f"Duplicate login_id '{login_id}' in CSV"
            elif email in seen_emails:
                error = f"Duplicate email '{email}' in CSV"
            else:
                seen_ids.add(login_id)
                seen_emails.add(email)

            validations.append(CSVRowValidation(
                row_number=idx,
                full_name=name,
                login_id=login_id,
                email=email,
                role=role,
                is_valid=error is None,
                error=error,
            ))

        total = len(validations)
        invalid = sum(1 for v in validations if not v.is_valid)
        valid = total - invalid

        return CSVImportDryRunReport(
            total_rows=total,
            valid_rows=valid,
            invalid_rows=invalid,
            errors=validations,
        )

