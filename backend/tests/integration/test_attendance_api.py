import uuid
from datetime import date
import pytest
from httpx import AsyncClient, ASGITransport

from app.main import app
from app.core.security import create_access_token, get_password_hash
from app.models.enums import AttendanceStatus, EnrollmentStatus, UserRole
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
from app.models.platform import PolicySetting


@pytest.mark.asyncio
async def test_complete_attendance_lifecycle(db_session, override_get_db):
    # 1. Setup seed data
    pol = PolicySetting(key="attendance_correction_window_days", value="7", description="Window")
    db_session.add(pol)

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

    # Teacher setup
    teacher_user = User(
        login_id="TEA001",
        email="teacher@campusone.edu",
        full_name="Prof. Turing",
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

    # Student setup
    student_user = User(
        login_id="STU001",
        email="student@campusone.edu",
        full_name="Student John",
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
        # 2. Teacher opens attendance session
        open_payload = {
            "section_id": str(section.id),
            "subject_id": str(subject.id),
            "session_date": "2026-10-29",
            "sequence_no": 1,
        }
        res = await ac.post(
            "/api/v1/attendance/sessions",
            json=open_payload,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert res.status_code == 201
        session_data = res.json()
        assert session_data["status"] == "OPEN"
        assert len(session_data["records"]) == 1
        assert session_data["records"][0]["status"] == "NOT_MARKED"

        session_id = session_data["id"]
        record_id = session_data["records"][0]["id"]

        # Duplicate session should return 409 session_exists
        dup_res = await ac.post(
            "/api/v1/attendance/sessions",
            json=open_payload,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert dup_res.status_code == 409
        assert dup_res.json()["error"]["code"] == "session_exists"

        # 3. Teacher saves marks (Student is PRESENT)
        save_payload = {
            "records": [{"student_id": str(student_prof.id), "status": "PRESENT"}]
        }
        save_res = await ac.put(
            f"/api/v1/attendance/sessions/{session_id}/records",
            json=save_payload,
            headers={"Authorization": f"Bearer {teacher_token}", "Idempotency-Key": str(uuid.uuid4())},
        )
        assert save_res.status_code == 200
        assert save_res.json()["saved"] == 1
        assert save_res.json()["not_marked"] == 0

        # Before finalization, student summary shows "No attendance data"
        sum_pre = await ac.get(
            "/api/v1/attendance/summary",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert sum_pre.status_code == 200
        assert sum_pre.json()["display_text"] == "No attendance data"

        # 4. Teacher finalizes session
        fin_res = await ac.post(
            f"/api/v1/attendance/sessions/{session_id}/finalize",
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert fin_res.status_code == 200
        assert fin_res.json()["status"] == "FINALIZED"

        # After finalization, student summary shows 100.0%
        sum_post = await ac.get(
            "/api/v1/attendance/summary",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert sum_post.status_code == 200
        assert sum_post.json()["overall_percentage"] == 100.0
        assert sum_post.json()["display_text"] == "100.0%"
        assert sum_post.json()["is_low"] is False

        # 5. Teacher corrects record (changes to ABSENT with reason)
        corr_payload = {
            "new_status": "ABSENT",
            "reason": "Student left early with permission",
        }
        corr_res = await ac.post(
            f"/api/v1/attendance/records/{record_id}/corrections",
            json=corr_payload,
            headers={"Authorization": f"Bearer {teacher_token}"},
        )
        assert corr_res.status_code == 201
        assert corr_res.json()["requires_admin_approval"] is False
        assert corr_res.json()["status"] == "APPROVED"

        # After correction, student summary updates to 0.0% (and marked low)
        sum_corr = await ac.get(
            "/api/v1/attendance/summary",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert sum_corr.status_code == 200
        assert sum_corr.json()["overall_percentage"] == 0.0
        assert sum_corr.json()["is_low"] is True

