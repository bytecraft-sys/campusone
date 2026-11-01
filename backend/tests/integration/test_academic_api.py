import io
import uuid
from datetime import date, time
import pytest
from httpx import AsyncClient, ASGITransport

from app.main import app
from app.core.security import create_access_token, get_password_hash
from app.models.enums import UserRole
from app.models.identity import User


@pytest.mark.asyncio
async def test_academic_structure_and_timetable_flow(db_session, override_get_db):
    admin_user = User(
        login_id="ADMIN_ROOT",
        email="admin.root@campusone.edu",
        full_name="Root Admin",
        hashed_password=get_password_hash("AdminPass123!"),
        role=UserRole.ADMIN,
        is_active=True,
    )
    db_session.add(admin_user)

    student_user = User(
        login_id="STU_NORMAL",
        email="student.normal@campusone.edu",
        full_name="Normal Student",
        hashed_password=get_password_hash("StudentPass123!"),
        role=UserRole.STUDENT,
        is_active=True,
    )
    db_session.add(student_user)
    await db_session.commit()

    admin_token = create_access_token(subject=admin_user.id, role=admin_user.role.value)
    student_token = create_access_token(subject=student_user.id, role=student_user.role.value)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Non-admin forbidden from admin endpoints
        unauth_res = await ac.post(
            "/api/v1/admin/departments",
            json={"code": "MECH", "name": "Mechanical"},
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert unauth_res.status_code == 403

        # 2. Admin creates Academic Year
        ay_res = await ac.post(
            "/api/v1/admin/academic-years",
            json={"name": "2026-2027", "start_date": "2026-08-01", "end_date": "2027-05-31", "is_current": True},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert ay_res.status_code == 201
        ay_id = ay_res.json()["id"]

        # 3. Admin creates Department
        dept_res = await ac.post(
            "/api/v1/admin/departments",
            json={"code": "CSE", "name": "Computer Science and Engineering"},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert dept_res.status_code == 201
        dept_id = dept_res.json()["id"]

        # 4. Admin creates Course
        course_res = await ac.post(
            "/api/v1/admin/courses",
            json={"department_id": dept_id, "code": "BTECH-CSE", "name": "B.Tech CSE", "total_semesters": 8},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert course_res.status_code == 201
        course_id = course_res.json()["id"]

        # 5. Admin creates Subject
        subj_res = await ac.post(
            "/api/v1/admin/subjects",
            json={"code": "CS301", "name": "Database Systems", "credit_hours": 4},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert subj_res.status_code == 201
        subj_id = subj_res.json()["id"]

        # 6. Admin creates Section
        sec_res = await ac.post(
            "/api/v1/admin/sections",
            json={"course_id": course_id, "academic_year_id": ay_id, "semester_no": 5, "name": "A"},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert sec_res.status_code == 201
        sec_id = sec_res.json()["id"]

        # 7. Admin provisions Teacher account (with single-use 72h link)
        teacher_create_res = await ac.post(
            "/api/v1/admin/users",
            json={
                "full_name": "Prof. Donald Knuth",
                "login_id": "TEA200",
                "email": "knuth@campusone.edu",
                "role": "TEACHER",
                "department_id": dept_id,
            },
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert teacher_create_res.status_code == 201
        t_data = teacher_create_res.json()
        assert t_data["login_id"] == "TEA200"
        assert "activation_token" in t_data
        assert "activation_link" in t_data

        # 8. Admin creates Time Slot
        slot_res = await ac.post(
            "/api/v1/admin/time-slots",
            json={"day_of_week": 0, "start_time": "09:00:00", "end_time": "10:00:00", "label": "Slot 1"},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert slot_res.status_code == 201
        slot_id = slot_res.json()["id"]

        # Fetch teacher profile id for allocations
        from app.models.academic import TeacherProfile
        from sqlalchemy import select
        t_prof = (await db_session.execute(select(TeacherProfile).where(TeacherProfile.user_id == t_data["user_id"]))).scalar_one()

        # 9. Admin schedules timetable entry
        tt_res = await ac.post(
            "/api/v1/admin/timetable",
            json={
                "section_id": sec_id,
                "subject_id": subj_id,
                "teacher_id": str(t_prof.id),
                "time_slot_id": slot_id,
            },
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert tt_res.status_code == 201

        # 10. Conflict detection: schedule teacher in same time slot -> 409 teacher_schedule_conflict
        tt_dup = await ac.post(
            "/api/v1/admin/timetable",
            json={
                "section_id": sec_id,
                "subject_id": subj_id,
                "teacher_id": str(t_prof.id),
                "time_slot_id": slot_id,
            },
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert tt_dup.status_code == 409
        assert tt_dup.json()["error"]["code"] == "teacher_schedule_conflict"

        # 11. CSV dry-run validation report
        csv_content = (
            "full_name,login_id,email,role\n"
            "Alice Wonder,STU991,alice@campusone.edu,STUDENT\n"
            "Bob Marley,,bob@campusone.edu,STUDENT\n"
            "Charlie Brown,STU993,charlie@campusone.edu,INVALID_ROLE\n"
        )
        csv_res = await ac.post(
            "/api/v1/admin/users/csv-dry-run",
            files={"file": ("users.csv", io.BytesIO(csv_content.encode("utf-8")), "text/csv")},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert csv_res.status_code == 200
        report = csv_res.json()
        assert report["total_rows"] == 3
        assert report["valid_rows"] == 1
        assert report["invalid_rows"] == 2

