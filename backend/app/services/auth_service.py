import uuid
from datetime import datetime, timedelta, timezone
from typing import Optional, Tuple
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.exceptions import CampusOneException, ForbiddenException, ConflictException
from app.core.security import (
    create_access_token,
    create_refresh_token,
    get_password_hash,
    hash_token,
    validate_password_strength,
    verify_password,
)
from app.models.enums import UserRole
from app.models.identity import ActivationToken, AuthSession, ConsentRecord, User
from app.schemas.auth import (
    ActivationRequest,
    DevicePayload,
    LoginRequest,
    TokenResponse,
    UserResponse,
)


class AuthService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def authenticate_user(self, payload: LoginRequest, client_ip: Optional[str] = None) -> TokenResponse:
        """
        Authenticates credentials with Argon2id, enforces lockout on repeated failures,
        and generates rotating refresh tokens.
        """
        now = datetime.now(timezone.utc)
        
        # 1. Fetch user by login_id
        result = await self.db.execute(select(User).where(User.login_id == payload.login_id))
        user = result.scalar_one_or_none()

        if not user:
            raise CampusOneException(
                code="invalid_credentials",
                message="Invalid login ID or password.",
                status_code=401
            )

        # 2. Check brute force lockout
        if user.locked_until and user.locked_until > now:
            retry_after = int((user.locked_until - now).total_seconds())
            raise CampusOneException(
                code="too_many_attempts",
                message=f"Account temporarily locked due to multiple failed login attempts. Retry after {retry_after} seconds.",
                status_code=429,
                details=[f"Retry-After: {retry_after}"]
            )

        # 3. Verify password
        is_valid = verify_password(payload.password, user.hashed_password)
        if not is_valid:
            user.failed_login_attempts += 1
            if user.failed_login_attempts >= 5:
                # Lockout for 15 minutes
                user.locked_until = now + timedelta(minutes=15)
            await self.db.commit()

            if user.failed_login_attempts >= 5:
                raise CampusOneException(
                    code="too_many_attempts",
                    message="Too many failed attempts. Account locked for 15 minutes.",
                    status_code=429
                )
            raise CampusOneException(
                code="invalid_credentials",
                message="Invalid login ID or password.",
                status_code=401
            )

        # 4. Check active status
        if not user.is_active:
            raise ForbiddenException(
                code="account_inactive",
                message="Account has been deactivated. Please contact your administrator."
            )

        # Reset failed attempts on success
        user.failed_login_attempts = 0
        user.locked_until = None

        # 5. Create new session and tokens
        session_id = uuid.uuid4()
        refresh_token = create_refresh_token(subject=user.id, session_id=str(session_id))
        refresh_token_hash = hash_token(refresh_token)
        refresh_expires = now + timedelta(days=settings.REFRESH_TOKEN_DAYS)

        new_session = AuthSession(
            id=session_id,
            user_id=user.id,
            device_platform=payload.device.platform,
            device_name=payload.device.name,
            refresh_token_hash=refresh_token_hash,
            expires_at=refresh_expires,
            is_revoked=False
        )
        self.db.add(new_session)
        await self.db.commit()

        access_token = create_access_token(subject=user.id, role=user.role.value)

        return TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="bearer",
            expires_in=settings.ACCESS_TOKEN_MINUTES * 60,
            user=UserResponse.model_validate(user)
        )

    async def activate_account(self, payload: ActivationRequest, client_ip: Optional[str] = None) -> bool:
        """
        Activates user account using single-use 72h token, sets Argon2id password,
        and records statutory DPDP consent.
        """
        if payload.new_password != payload.confirm_password:
            raise CampusOneException(code="password_mismatch", message="Passwords do not match.", status_code=400)

        is_strong, err_msg = validate_password_strength(payload.new_password)
        if not is_strong:
            raise CampusOneException(code="weak_password", message=err_msg or "Password is weak.", status_code=400)

        token_hash = hash_token(payload.token)
        result = await self.db.execute(
            select(ActivationToken).where(
                ActivationToken.hashed_token == token_hash,
                ActivationToken.used == False
            )
        )
        activation_record = result.scalar_one_or_none()
        
        if not activation_record:
            raise CampusOneException(
                code="invalid_activation_link",
                message="Activation link is invalid or has already been used.",
                status_code=400
            )

        now = datetime.now(timezone.utc)
        if activation_record.expires_at < now:
            raise CampusOneException(
                code="activation_link_expired",
                message="Activation link has expired (72-hour window passed). Request a new one from admin.",
                status_code=400
            )

        # Update user
        user_result = await self.db.execute(select(User).where(User.id == activation_record.user_id))
        user = user_result.scalar_one()

        user.hashed_password = get_password_hash(payload.new_password)
        user.is_active = True
        user.must_change_password = False
        activation_record.used = True

        # Record consent (DPDP compliance)
        consent = ConsentRecord(
            user_id=user.id,
            consent_type="privacy_notice_v1",
            agreed=True,
            guardian_consent=payload.guardian_consent,
            guardian_name=payload.guardian_name,
            ip_address=client_ip
        )
        self.db.add(consent)
        await self.db.commit()
        return True
