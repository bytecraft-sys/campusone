import uuid
from datetime import date, datetime, timedelta, timezone
import pytest
from httpx import AsyncClient, ASGITransport

from app.main import app
from app.core.security import create_access_token, get_password_hash
from app.models.enums import AssignmentStatus, EnrollmentStatus, SubmissionStatus, UserRole
from app.models.identity import User
from app.models.academic import (
    Department,
    AcademicYear,
    Course,
    Subject,
    Section,
    TeacherProfile,
    StudentProfile,
    Enrollment,
    TeachingAssignment,
)


@pytest.mark.asyncio
async def test_assignments_and_grading_flow(db_session, override_get_db):
    dept = Department(code="CSE", name="Computer Science")
    db_session.add(dept)
    await db_session.flush()

    ay = AcademicYear(name="2026-2027", start_date=date(2026, 8, 1), end_date=date(2027, 5, 31), is_current=True)
    db_session.add(ay)
    await db_session.flush()

    course = Course(department_id=dept.id, code="BTECH-CS", name="B.Tech CS", total_semesters=8)
    db_session.add(course)
    await db_session.flush()

    subject = Subject(code="CS301", name="DBMS", credit_hours=4)
    db_session.add(subject)
    await db_session.flush()

    section = Section(course_id=course.id, academic_year_id=ay.id, semester_no=5, name="A")
    db_session.add(section)
    await db_session.flush()

    # Teacher
    teacher_user = User(
        login_id="TEA001",
        email="teacher1@campusone.edu",
        full_name="Prof. Assigned",
        hashed_password=get_password_hash("TeacherPass123!"),
        role=UserRole.TEACHER,
        is_active=True,
    )
    db_session.add(teacher_user)
    await db_session.flush()

    teacher_prof = TeacherProfile(user_id=teacher_user.id, employee_code="T-001", department_id=dept.id)
    db_session.add(teacher_prof)
    await db_session.flush()

    db_session.add(TeachingAssignment(
        teacher_id=teacher_prof.id,
        section_id=section.id,
        subject_id=subject.id,
        academic_year_id=ay.id,
    ))

    # Student
    student_user = User(
        login_id="STU001",
        email="student@campusone.edu",
        full_name="Student Bob",
        hashed_password=get_password_hash("StudentPass123!"),
        role=UserRole.STUDENT,
        is_active=True,
    )
    db_session.add(student_user)
    await db_session.flush()

    student_prof = StudentProfile(
        user_id=student_user.id,
        admission_number="ADM-001",
        date_of_birth=date(2005, 1, 1),
    )
    db_session.add(student_prof)
    await db_session.flush()

    db_session.add(Enrollment(
        student_id=student_prof.id,
        section_id=section.id,
        academic_year_id=ay.id,
        status=EnrollmentStatus.ACTIVE,
    ))

    await db_session.commit()

    teacher_token = create_access_token(subject=teacher_user.id, role=teacher_user.role.value)
    student_token = create_access_token(subject=student_user.id, role=student_user.role.value)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Teacher creates assignment
        now = datetime.now(timezone.utc)
        due_at = now + timedelta(days=7)
        assign_payload = {
            "section_id": str(section.id),
            "subject_id": str(subject.id),
            "title": "SQL Query Optimization Lab",
            "instructions": "Submit queries and execution plans",
            "due_at": due_at.isoformat(),
            "max_marks": 20.0,
            "allow_late": True,
            "allow_resubmission": True,
            "status": "PUBLISHED"
        }
        res = await ac.post(
            "/api/v1/assignments",
            json=assign_payload,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert res.status_code == 201
        assignment_data = res.json()
        assignment_id = assignment_data["id"]
        assert assignment_data["title"] == "SQL Query Optimization Lab"

        # 2. Student lists assignments
        list_res = await ac.get(
            "/api/v1/assignments",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert list_res.status_code == 200
        assert len(list_res.json()) >= 1

        # 3. Student submits assignment (Version 1)
        sub1_payload = {
            "text_answer": "Select * from students where id = 1;",
            "file_ids": []
        }
        sub1_res = await ac.post(
            f"/api/v1/assignments/{assignment_id}/submissions",
            json=sub1_payload,
            headers={"Authorization": f"Bearer {student_token}", "Idempotency-Key": str(uuid.uuid4())},
        )
        assert sub1_res.status_code == 201
        sub1_data = sub1_res.json()
        assert sub1_data["version_no"] == 1
        assert sub1_data["is_current"] is True

        # 4. Student resubmits (Version 2)
        sub2_payload = {
            "text_answer": "Optimized: Select id, name from students where id = 1;",
            "file_ids": []
        }
        sub2_res = await ac.post(
            f"/api/v1/assignments/{assignment_id}/submissions",
            json=sub2_payload,
            headers={"Authorization": f"Bearer {student_token}", "Idempotency-Key": str(uuid.uuid4())},
        )
        assert sub2_res.status_code == 201
        sub2_data = sub2_res.json()
        assert sub2_data["version_no"] == 2
        assert sub2_data["is_current"] is True
        submission_id = sub2_data["id"]

        # 5. Teacher grades with invalid marks (> max_marks) -> 400 validation_error
        bad_grade = {"marks": 25.0, "feedback": "Exceeds max", "status": "GRADED"}
        bad_res = await ac.patch(
            f"/api/v1/submissions/{submission_id}/grade",
            json=bad_grade,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert bad_res.status_code == 400
        assert bad_res.json()["error"]["code"] == "validation_error"

        # 6. Teacher grades with valid marks -> 200 GRADED
        good_grade = {"marks": 18.5, "feedback": "Excellent query optimization", "status": "GRADED"}
        good_res = await ac.patch(
            f"/api/v1/submissions/{submission_id}/grade",
            json=good_grade,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert good_res.status_code == 200
        assert good_res.json()["marks"] == 18.5
        assert good_res.json()["status"] == "GRADED"
        assert good_res.json()["feedback"] == "Excellent query optimization"

