import uuid
from datetime import datetime, timezone
import pytest
from httpx import AsyncClient, ASGITransport

from app.main import app
from app.core.security import create_access_token, get_password_hash
from app.models.enums import UserRole
from app.models.identity import User
from app.models.platform import PolicySetting


@pytest.mark.asyncio
async def test_privacy_policies_and_audit_flow(db_session, override_get_db):
    admin_user = User(
        login_id="ADMIN_PRIV",
        email="admin.priv@campusone.edu",
        full_name="Privacy Officer",
        hashed_password=get_password_hash("AdminPass123!"),
        role=UserRole.ADMIN,
        is_active=True,
    )
    db_session.add(admin_user)

    student_user = User(
        login_id="STU_PRIV",
        email="student.priv@campusone.edu",
        full_name="Private Student",
        hashed_password=get_password_hash("StudentPass123!"),
        role=UserRole.STUDENT,
        is_active=True,
    )
    db_session.add(student_user)

    policy = PolicySetting(
        key="attendance_threshold",
        value="75",
        description="Minimum attendance threshold"
    )
    db_session.add(policy)

    await db_session.commit()

    admin_token = create_access_token(subject=admin_user.id, role=admin_user.role.value)
    student_token = create_access_token(subject=student_user.id, role=student_user.role.value)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Student submits DPDP data export request
        req_res = await ac.post(
            "/api/v1/me/data-requests",
            json={
                "request_type": "EXPORT",
                "details": "Export full student portfolio under India's DPDP Act"
            },
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert req_res.status_code == 201
        data_req = req_res.json()
        assert data_req["request_type"] == "EXPORT"
        assert data_req["status"] == "PENDING"
        assert "due_date" in data_req

        # 2. Student downloads their comprehensive data export
        export_res = await ac.get(
            "/api/v1/me/data-export",
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert export_res.status_code == 200
        export_data = export_res.json()
        assert export_data["login_id"] == "STU_PRIV"
        assert export_data["role"] == "STUDENT"

        # 3. Admin updates policy setting (e.g. raises threshold to 80%)
        pol_res = await ac.put(
            "/api/v1/admin/policies/attendance_threshold",
            json={"value": "80"},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert pol_res.status_code == 200
        pol_data = pol_res.json()
        assert pol_data["value"] == "80"
        assert pol_data["old_value"] == "75"

        # 4. Admin queries append-only audit logs
        audit_res = await ac.get(
            "/api/v1/admin/audit-logs?action=policy.update",
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert audit_res.status_code == 200
        audit_logs = audit_res.json()
        assert len(audit_logs) >= 1
        assert audit_logs[0]["action"] == "policy.update"
        assert audit_logs[0]["resource_id"] == "attendance_threshold"
        assert audit_logs[0]["old_value"] == "75"
        assert audit_logs[0]["new_value"] == "80"

        # 5. Student submits erasure request and Admin processes it
        erase_req = await ac.post(
            "/api/v1/me/data-requests",
            json={"request_type": "ERASURE", "details": "Graduated student requesting right to be forgotten"},
            headers={"Authorization": f"Bearer {student_token}"},
        )
        assert erase_req.status_code == 201
        erase_id = erase_req.json()["id"]

        process_res = await ac.patch(
            f"/api/v1/admin/data-requests/{erase_id}",
            json={"status": "COMPLETED", "details": "Records anonymized and account deactivated"},
            headers={"Authorization": f"Bearer {admin_token}"},
        )
        assert process_res.status_code == 200
        assert process_res.json()["status"] == "COMPLETED"

        # Verify user account was anonymized
        from sqlalchemy import select
        anonymized_user = (await db_session.execute(select(User).where(User.id == student_user.id))).scalar_one()
        assert "Anonymized" in anonymized_user.full_name
        assert anonymized_user.is_active is False

