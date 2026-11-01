import hashlib
import secrets
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, Optional, Union
import jwt
from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError

from app.core.config import settings

# Initialize Argon2id password hasher with specified parameters
ph = PasswordHasher(
    time_cost=settings.ARGON2_TIME_COST,
    memory_cost=settings.ARGON2_MEMORY_COST,
    parallelism=settings.ARGON2_PARALLELISM
)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verifies a plain password against the stored Argon2id hash."""
    try:
        return ph.verify(hashed_password, plain_password)
    except VerifyMismatchError:
        return False
    except Exception:
        return False


def get_password_hash(password: str) -> str:
    """Hashes a password using Argon2id."""
    return ph.hash(password)


def validate_password_strength(password: str) -> tuple[bool, Optional[str]]:
    """Enforces minimum 10 characters and strength criteria."""
    if len(password) < 10:
        return False, "Password must be at least 10 characters long."
    # Disallow common weak passwords
    common_weak = {"password123", "campusone123", "admin12345", "1234567890", "qwertyuiop"}
    if password.lower() in common_weak:
        return False, "Password is too common and insecure."
    return True, None


def create_access_token(subject: Union[str, Any], role: str, expires_delta: Optional[timedelta] = None) -> str:
    """Creates a short-lived access JWT token (default 15 minutes)."""
    now = datetime.now(timezone.utc)
    if expires_delta:
        expire = now + expires_delta
    else:
        expire = now + timedelta(minutes=settings.ACCESS_TOKEN_MINUTES)
    
    to_encode = {
        "sub": str(subject),
        "role": role,
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
        "type": "access"
    }
    encoded_jwt = jwt.encode(to_encode, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)
    return encoded_jwt


def create_refresh_token(subject: Union[str, Any], session_id: str, expires_delta: Optional[timedelta] = None) -> str:
    """Creates a rotating refresh token (default 30 days)."""
    now = datetime.now(timezone.utc)
    if expires_delta:
        expire = now + expires_delta
    else:
        expire = now + timedelta(days=settings.REFRESH_TOKEN_DAYS)
        
    to_encode = {
        "sub": str(subject),
        "session_id": session_id,
        "token_family": secrets.token_hex(16),
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
        "type": "refresh"
    }
    return jwt.encode(to_encode, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


def generate_activation_token() -> tuple[str, str, datetime]:
    """
    Generates a secure single-use 72-hour activation token.
    Returns (raw_token, hashed_token, expires_at).
    """
    raw_token = secrets.token_urlsafe(32)
    hashed_token = hashlib.sha256(raw_token.encode("utf-8")).hexdigest()
    expires_at = datetime.now(timezone.utc) + timedelta(hours=settings.ACTIVATION_TOKEN_HOURS)
    return raw_token, hashed_token, expires_at


def hash_token(token: str) -> str:
    """Hashes an activation or refresh token for secure database lookup."""
    return hashlib.sha256(token.encode("utf-8")).hexdigest()
