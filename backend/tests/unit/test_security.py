import pytest
from datetime import datetime, timezone
import jwt

from app.core.config import settings
from app.core.security import (
    create_access_token,
    create_refresh_token,
    generate_activation_token,
    get_password_hash,
    hash_token,
    validate_password_strength,
    verify_password,
)


def test_password_strength():
    # Less than 10 characters
    valid, err = validate_password_strength("Short1!")
    assert not valid
    assert "at least 10 characters" in err

    # Disallowed common passwords
    valid, err = validate_password_strength("password123")
    assert not valid
    assert "too common" in err

    # Valid strong password
    valid, err = validate_password_strength("ComplexPassphrase2026!")
    assert valid
    assert err is None


def test_argon2_hashing():
    raw_password = "SecureCampusPassword123"
    hashed = get_password_hash(raw_password)
    assert hashed != raw_password
    assert hashed.startswith("$argon2id$")

    # Verify matching password
    assert verify_password(raw_password, hashed) is True

    # Verify mismatch
    assert verify_password("WrongPassword123", hashed) is False


def test_access_token_creation_and_payload():
    user_id = "550e8400-e29b-41d4-a716-446655440000"
    role = "STUDENT"
    token = create_access_token(subject=user_id, role=role)

    payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
    assert payload["sub"] == user_id
    assert payload["role"] == role
    assert payload["type"] == "access"
    assert "exp" in payload
    assert "iat" in payload


def test_activation_token_generation():
    raw_token, hashed, expires_at = generate_activation_token()
    assert len(raw_token) > 20
    assert hash_token(raw_token) == hashed
    now = datetime.now(timezone.utc)
    delta_hours = (expires_at - now).total_seconds() / 3600
    assert 71 <= delta_hours <= 73
