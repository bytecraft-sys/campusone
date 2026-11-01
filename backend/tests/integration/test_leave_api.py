import uuid
from datetime import date
import pytest
from httpx import AsyncClient, ASGITransport

from app.main import app
from app.core.security import create_access_token, get_password_hash
from app.models.enums import EnrollmentStatus, LeaveStatus, UserRole
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
async def test_complete_leave_workflow(db_session, override_get_db):
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

    # Assigned Teacher
    teacher1_user = User(
        login_id="TEA001",
        email="teacher1@campusone.edu",
        full_name="Prof. Assigned",
        hashed_password=get_password_hash("TeacherPass123!"),
        role=UserRole.TEACHER,
        is_active=True,
    )
    db_session.add(teacher1_user)
    await db_session.flush()

    teacher1_prof = TeacherProfile(user_id=teacher1_user.id, employee_code="T-001", department_id=dept.id)
    db_session.add(teacher1_prof)
    await db_session.flush()

    db_session.add(TeachingAssignment(
        teacher_id=teacher1_prof.id,
        section_id=section.id,
        subject_id=subject.id,
        academic_year_id=ay.id,
    ))

    # Unassigned Teacher
    teacher2_user = User(
        login_id="TEA002",
        email="teacher2@campusone.edu",
        full_name="Prof. Other",
        hashed_password=get_password_hash("TeacherPass123!"),
        role=UserRole.TEACHER,
        is_active=True,
    )
    db_session.add(teacher2_user)
    await db_session.flush()

    teacher2_prof = TeacherProfile(user_id=teacher2_user.id, employee_code="T-002", department_id=dept.id)
    db_session.add(teacher2_prof)
    await db_session.flush()

    # Student
    student_user = User(
        login_id="STU001",
        email="student@campusone.edu",
        full_name="Student Jane",
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

    teacher1_token = create_access_token(subject=teacher1_user.id, role=teacher1_user.role.value)
    teacher2_token = create_access_token(subject=teacher2_user.id, role=teacher2_user.role.value)
    student_token = create_access_token(subject=student_user.id, role=student_user.role.value)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Student applies for leave
        apply_payload = {
            "from_date": "2026-11-10",
            "to_date": "2026-11-12",
            "reason": "Family emergency and travel",
            "attachment_file_ids": []
        }
        res = await ac.post(
            "/api/v1/leaves",
            json=apply_payload,
            headers={"Authorization": f"Bearer {student_token}", "Idempotency-Key": str(uuid.uuid4())},
        )
        assert res.status_code == 201
        leave_data = res.json()
        assert leave_data["status"] == "PENDING"
        assert leave_data["assigned_teacher_id"] == str(teacher1_prof.id)
        assert len(leave_data["decisions"]) == 1
        assert leave_data["decisions"][0]["decision"] == "APPLIED"

        leave_id = leave_data["id"]

        # 2. Overlapping leave fails with 409
        dup_res = await ac.post(
            "/api/v1/leaves",
            json=apply_payload,
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert dup_res.status_code == 409
        assert dup_res.json()["error"]["code"] == "overlapping_leave"

        # 3. Unassigned teacher tries to approve -> 403 not_assigned_teacher
        decide_payload = {"decision": "APPROVED", "comments": "Approved"}
        unauth_res = await ac.patch(
            f"/api/v1/leaves/{leave_id}/decision",
            json=decide_payload,
            headers={"Authorization": f"Bearer {teacher2_token}"},
        )
        assert unauth_res.status_code == 403
        assert unauth_res.json()["error"]["code"] == "not_assigned_teacher"

        # 4. Assigned teacher approves leave -> 200 APPROVED
        approve_res = await ac.patch(
            f"/api/v1/leaves/{leave_id}/decision",
            json=decide_payload,
            headers={"Authorization": f"Bearer {teacher1_token}"},
        )
        assert approve_res.status_code == 200
        assert approve_res.json()["status"] == "APPROVED"
        assert len(approve_res.json()["decisions"]) == 2

        # 5. Already decided leave cannot be modified -> 409 already_decided
        repeat_res = await ac.patch(
            f"/api/v1/leaves/{leave_id}/decision",
            json=decide_payload,
            headers={"Authorization": f"Bearer {teacher1_token}"},
        )
        assert repeat_res.status_code == 409
        assert repeat_res.json()["error"]["code"] == "already_decided"

        # 6. Apply another leave and cancel it
        apply2_payload = {
            "from_date": "2026-11-20",
            "to_date": "2026-11-21",
            "reason": "Dentist appointment",
        }
        res2 = await ac.post(
            "/api/v1/leaves",
            json=apply2_payload,
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert res2.status_code == 201
        leave2_id = res2.json()["id"]

        cancel_res = await ac.post(
            f"/api/v1/leaves/{leave2_id}/cancel",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert cancel_res.status_code == 200
        assert cancel_res.json()["status"] == "CANCELLED"

