import asyncio
import uuid
from datetime import date, datetime, timedelta, timezone
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.security import get_password_hash
from app.db.session import AsyncSessionLocal, engine
from app.db.base import Base
from app.models.enums import (
    UserRole,
    EnrollmentStatus,
    AttendanceStatus,
    SessionStatus,
    AssignmentStatus,
    LeaveStatus,
)
from app.models.identity import User
from app.models.academic import (
    Department,
    AcademicYear,
    Course,
    Subject,
    CourseSubject,
    Section,
    TeacherProfile,
    StudentProfile,
    Enrollment,
    TeachingAssignment,
)
from app.models.attendance import AttendanceSession, AttendanceRecord
from app.models.assignments import Assignment
from app.models.leave import LeaveRequest
from app.models.platform import PolicySetting


async def seed_data():
    """Seeds the initial institutional setup, users, attendance, leaves, and coursework."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    async with AsyncSessionLocal() as session:
        # 1. Seed Default Policies
        policies = [
            ("attendance_threshold", "75", "Minimum attendance percentage required before alerts trigger"),
            ("attendance_correction_window_days", "7", "Days within which teachers can correct attendance without admin sign-off"),
            ("late_submission_penalty_percent", "10", "Default late submission penalty percentage"),
            ("leave_escalation_days", "3", "Days after which unanswered leave requests are escalated to admin"),
        ]
        for key, val, desc in policies:
            existing = await session.execute(select(PolicySetting).where(PolicySetting.key == key))
            if not existing.scalar_one_or_none():
                session.add(PolicySetting(key=key, value=val, description=desc))

        # 2. Seed Superadmin User
        admin_login = "ADMIN001"
        existing_admin = await session.execute(select(User).where(User.login_id == admin_login))
        admin_user = existing_admin.scalar_one_or_none()
        if not admin_user:
            admin_user = User(
                login_id=admin_login,
                email="admin@campusone.edu",
                full_name="System Administrator",
                hashed_password=get_password_hash("AdminPass123!"),
                role=UserRole.ADMIN,
                is_active=True,
                must_change_password=False,
            )
            session.add(admin_user)
            await session.flush()
            print(f"Created default admin: {admin_login} / AdminPass123!")

        # 3. Seed Department
        dept_result = await session.execute(select(Department).where(Department.code == "CSE"))
        dept = dept_result.scalar_one_or_none()
        if not dept:
            dept = Department(code="CSE", name="Computer Science and Engineering")
            session.add(dept)
            await session.flush()

        # 4. Seed Academic Year
        ay_result = await session.execute(select(AcademicYear).where(AcademicYear.name == "2026-2027"))
        ay = ay_result.scalar_one_or_none()
        if not ay:
            ay = AcademicYear(
                name="2026-2027",
                start_date=date(2026, 8, 1),
                end_date=date(2027, 5, 31),
                is_current=True,
            )
            session.add(ay)
            await session.flush()

        # 5. Seed Course
        course_result = await session.execute(select(Course).where(Course.code == "BT-CSE"))
        course = course_result.scalar_one_or_none()
        if not course:
            course = Course(
                department_id=dept.id,
                code="BT-CSE",
                name="B.Tech Computer Science and Engineering",
                total_semesters=8,
            )
            session.add(course)
            await session.flush()

        # 6. Seed Subjects
        subject_defs = [
            ("CS301", "Database Management Systems", 4),
            ("CS302", "Operating Systems", 4),
            ("CS303", "Data Structures & Algorithms", 4),
        ]
        created_subjects = []
        for code, name, credits in subject_defs:
            subj_result = await session.execute(select(Subject).where(Subject.code == code))
            subj = subj_result.scalar_one_or_none()
            if not subj:
                subj = Subject(code=code, name=name, credit_hours=credits)
                session.add(subj)
                await session.flush()
                session.add(CourseSubject(
                    course_id=course.id,
                    subject_id=subj.id,
                    semester_no=5,
                    is_elective=False,
                ))
            created_subjects.append(subj)

        # 7. Seed Section
        sec_result = await session.execute(
            select(Section).where(
                Section.course_id == course.id,
                Section.academic_year_id == ay.id,
                Section.semester_no == 5,
                Section.name == "A",
            )
        )
        section = sec_result.scalar_one_or_none()
        if not section:
            section = Section(
                course_id=course.id,
                academic_year_id=ay.id,
                semester_no=5,
                name="A",
            )
            session.add(section)
            await session.flush()

        # 8. Seed Teacher (Alan Turing)
        teacher_login = "TEA101"
        existing_teacher = await session.execute(select(User).where(User.login_id == teacher_login))
        teacher_user = existing_teacher.scalar_one_or_none()
        if not teacher_user:
            teacher_user = User(
                login_id=teacher_login,
                email="teacher.cse@campusone.edu",
                full_name="Prof. Alan Turing",
                hashed_password=get_password_hash("TeacherPass123!"),
                role=UserRole.TEACHER,
                is_active=True,
                must_change_password=False,
            )
            session.add(teacher_user)
            await session.flush()

            teacher_profile = TeacherProfile(
                user_id=teacher_user.id,
                employee_code="EMP-CSE-001",
                department_id=dept.id,
            )
            session.add(teacher_profile)
            await session.flush()

            session.add(TeachingAssignment(
                teacher_id=teacher_profile.id,
                section_id=section.id,
                subject_id=created_subjects[0].id,
                academic_year_id=ay.id,
            ))
        else:
            tp_result = await session.execute(select(TeacherProfile).where(TeacherProfile.user_id == teacher_user.id))
            teacher_profile = tp_result.scalar_one()

        # 9. Seed Students Cohort
        students_meta = [
            ("STU2026001", "Shrey Student", "student.shrey@campusone.edu", "ADM-2026-001"),
            ("STU2026002", "Aarav Sharma", "aarav@campusone.edu", "ADM-2026-002"),
            ("STU2026003", "Ananya Verma", "ananya@campusone.edu", "ADM-2026-003"),
            ("STU2026004", "Dev Patel", "dev@campusone.edu", "ADM-2026-004"),
            ("STU2026005", "Isha Gupta", "isha@campusone.edu", "ADM-2026-005"),
        ]
        student_profiles = []
        for s_login, s_name, s_email, s_adm in students_meta:
            existing_s = await session.execute(select(User).where(User.login_id == s_login))
            s_user = existing_s.scalar_one_or_none()
            if not s_user:
                s_user = User(
                    login_id=s_login,
                    email=s_email,
                    full_name=s_name,
                    hashed_password=get_password_hash("StudentPass123!"),
                    role=UserRole.STUDENT,
                    is_active=True,
                    must_change_password=False,
                )
                session.add(s_user)
                await session.flush()

                s_prof = StudentProfile(
                    user_id=s_user.id,
                    admission_number=s_adm,
                    date_of_birth=date(2005, 5, 15),
                    guardian_name=f"Guardian of {s_name}",
                    guardian_phone="+919876543210",
                )
                session.add(s_prof)
                await session.flush()

                session.add(Enrollment(
                    student_id=s_prof.id,
                    section_id=section.id,
                    academic_year_id=ay.id,
                    status=EnrollmentStatus.ACTIVE,
                ))
                student_profiles.append(s_prof)
            else:
                sp_res = await session.execute(select(StudentProfile).where(StudentProfile.user_id == s_user.id))
                student_profiles.append(sp_res.scalar_one())

        main_student = student_profiles[0]

        # 10. Seed Finalized Attendance Sessions & Records (Target: 82% Overall: 41 Present, 9 Absent)
        existing_sess = await session.execute(select(AttendanceSession).limit(1))
        if not existing_sess.scalar_one_or_none():
            print("Seeding live attendance history...")
            start_d = date(2026, 9, 1)
            # 50 lecture sessions
            for i in range(50):
                sess_date = start_d + timedelta(days=(i * 1))
                subj = created_subjects[i % len(created_subjects)]
                att_session = AttendanceSession(
                    section_id=section.id,
                    subject_id=subj.id,
                    teacher_id=teacher_profile.id,
                    session_date=sess_date,
                    sequence_no=1,
                    status=SessionStatus.FINALIZED,
                    finalized_at=datetime.now(timezone.utc),
                )
                session.add(att_session)
                await session.flush()

                # Determine if main student is present (41 present, 9 absent)
                is_present = (i % 6 != 0)  # ~82% presence
                status_main = AttendanceStatus.PRESENT if is_present else AttendanceStatus.ABSENT

                for s_prof in student_profiles:
                    s_status = status_main if s_prof.id == main_student.id else AttendanceStatus.PRESENT
                    session.add(AttendanceRecord(
                        session_id=att_session.id,
                        student_id=s_prof.id,
                        status=s_status,
                    ))

        # 11. Seed Live Published Assignments
        existing_assign = await session.execute(select(Assignment).limit(1))
        if not existing_assign.scalar_one_or_none():
            print("Seeding live assignments...")
            assignments_data = [
                ("Binary Search Tree Implementation", created_subjects[2].id, "Implement AVL balancing and search operations.", datetime.now(timezone.utc) + timedelta(days=1), 20.0),
                ("ER Diagram for Hospital System", created_subjects[0].id, "Design normalized 3NF relational schema with constraints.", datetime.now(timezone.utc) + timedelta(days=3), 15.0),
                ("CPU Scheduling Simulator", created_subjects[1].id, "Simulate Round Robin and Priority scheduling algorithms.", datetime.now(timezone.utc) + timedelta(days=5), 30.0),
            ]
            for a_title, a_subj_id, a_inst, a_due, a_max in assignments_data:
                session.add(Assignment(
                    section_id=section.id,
                    subject_id=a_subj_id,
                    teacher_id=teacher_profile.id,
                    title=a_title,
                    instructions=a_inst,
                    due_at=a_due,
                    max_marks=a_max,
                    status=AssignmentStatus.PUBLISHED,
                ))

        # 12. Seed Live Leave Requests
        existing_leave = await session.execute(select(LeaveRequest).limit(1))
        if not existing_leave.scalar_one_or_none():
            print("Seeding live leave requests...")
            session.add(LeaveRequest(
                student_id=main_student.id,
                assigned_teacher_id=teacher_profile.id,
                from_date=date(2026, 11, 2),
                to_date=date(2026, 11, 3),
                reason="Viral fever and rest prescribed by doctor",
                status=LeaveStatus.PENDING,
            ))

        await session.commit()
        print("Database seed completed with live records!")


if __name__ == "__main__":
    asyncio.run(seed_data())
