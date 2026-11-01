import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.core.security import generate_activation_token, get_password_hash
from app.models.enums import UserRole
from app.models.identity import User, ActivationToken


@pytest.mark.asyncio
async def test_health_check():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        response = await ac.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "version" in data


@pytest.mark.asyncio
async def test_auth_login_success_and_me(db_session, override_get_db):
    # 1. Create a test user in DB
    user = User(
        login_id="TEST_TEACHER",
        email="teacher.test@campusone.edu",
        full_name="Prof. Test Teacher",
        hashed_password=get_password_hash("TeacherSecret123!"),
        role=UserRole.TEACHER,
        is_active=True,
    )
    db_session.add(user)
    await db_session.commit()

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 2. Login
        login_payload = {
            "login_id": "TEST_TEACHER",
            "password": "TeacherSecret123!",
            "device": {"platform": "web", "name": "Chrome Test Runner"}
        }
        resp = await ac.post("/api/v1/auth/login", json=login_payload)
        assert resp.status_code == 200
        data = resp.json()
        assert "access_token" in data
        assert "refresh_token" in data
        assert data["user"]["login_id"] == "TEST_TEACHER"
        assert data["user"]["role"] == "TEACHER"

        token = data["access_token"]

        # 3. Access protected /me
        me_resp = await ac.get(
            "/api/v1/auth/me",
            headers={"Authorization": f"Bearer {token}"}
        )
        assert me_resp.status_code == 200
        me_data = me_resp.json()
        assert me_data["login_id"] == "TEST_TEACHER"
        assert me_data["role"] == "TEACHER"


@pytest.mark.asyncio
async def test_auth_login_invalid_password(db_session, override_get_db):
    user = User(
        login_id="TEST_USER",
        email="user.test@campusone.edu",
        full_name="Test User",
        hashed_password=get_password_hash("CorrectPassword123!"),
        role=UserRole.STUDENT,
        is_active=True,
    )
    db_session.add(user)
    await db_session.commit()

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        login_payload = {
            "login_id": "TEST_USER",
            "password": "WrongPassword999!",
            "device": {"platform": "android", "name": "Pixel"}
        }
        resp = await ac.post("/api/v1/auth/login", json=login_payload)
        assert resp.status_code == 401
        err_data = resp.json()
        assert err_data["error"]["code"] == "invalid_credentials"
        assert "X-Request-Id" in resp.headers


@pytest.mark.asyncio
async def test_account_activation(db_session, override_get_db):
    user = User(
        login_id="NEW_STUDENT",
        email="new.student@campusone.edu",
        full_name="New Student",
        hashed_password=get_password_hash("PlaceholderTemporaryHash"),
        role=UserRole.STUDENT,
        is_active=False,
    )
    db_session.add(user)
    await db_session.flush()

    raw_token, hashed_token, expires_at = generate_activation_token()
    activation_record = ActivationToken(
        user_id=user.id,
        hashed_token=hashed_token,
        expires_at=expires_at,
        used=False
    )
    db_session.add(activation_record)
    await db_session.commit()

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        activate_payload = {
            "token": raw_token,
            "new_password": "NewStrongPass2026!",
            "confirm_password": "NewStrongPass2026!",
            "guardian_consent": True,
            "guardian_name": "Parent Name"
        }
        resp = await ac.post("/api/v1/auth/activate", json=activate_payload)
        assert resp.status_code == 200
        assert resp.json()["status"] == "success"

        # Now login should succeed with new password
        login_payload = {
            "login_id": "NEW_STUDENT",
            "password": "NewStrongPass2026!",
            "device": {"platform": "android", "name": "Pixel"}
        }
        login_resp = await ac.post("/api/v1/auth/login", json=login_payload)
        assert login_resp.status_code == 200

