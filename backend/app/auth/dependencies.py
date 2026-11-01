import uuid
from typing import List, Optional
from fastapi import Depends, HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
import jwt
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.exceptions import ForbiddenException, CampusOneException
from app.db.session import get_db
from app.models.enums import UserRole
from app.models.identity import User

security_scheme = HTTPBearer(auto_error=False)


async def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Security(security_scheme),
    db: AsyncSession = Depends(get_db)
) -> User:
    """Validates the Bearer access token and retrieves the current authenticated user."""
    if not credentials:
        raise CampusOneException(
            code="missing_credentials",
            message="Authorization bearer token required.",
            status_code=status.HTTP_401_UNAUTHORIZED
        )

    token = credentials.credentials
    try:
        payload = jwt.decode(
            token,
            settings.JWT_SECRET,
            algorithms=[settings.JWT_ALGORITHM]
        )
        token_type = payload.get("type")
        if token_type != "access":
            raise CampusOneException(
                code="invalid_token_type",
                message="Expected an access token.",
                status_code=status.HTTP_401_UNAUTHORIZED
            )
            
        user_id = payload.get("sub")
        if not user_id:
            raise CampusOneException(
                code="invalid_token",
                message="Token subject missing.",
                status_code=status.HTTP_401_UNAUTHORIZED
            )
    except jwt.ExpiredSignatureError:
        raise CampusOneException(
            code="token_expired",
            message="Access token has expired.",
            status_code=status.HTTP_401_UNAUTHORIZED
        )
    except jwt.PyJWTError:
        raise CampusOneException(
            code="invalid_token",
            message="Invalid token signature or formatting.",
            status_code=status.HTTP_401_UNAUTHORIZED
        )

    # Fetch active user from database
    result = await db.execute(select(User).where(User.id == uuid.UUID(user_id)))
    user = result.scalar_one_or_none()
    
    if not user:
        raise CampusOneException(
            code="user_not_found",
            message="User account no longer exists.",
            status_code=status.HTTP_401_UNAUTHORIZED
        )

    if not user.is_active:
        raise ForbiddenException(
            code="account_inactive",
            message="User account has been deactivated."
        )

    return user


def require_role(*roles: UserRole):
    """Factory creating an endpoint dependency checking if the user belongs to allowed roles."""
    async def role_checker(current_user: User = Depends(get_current_user)) -> User:
        if current_user.role not in roles:
            raise ForbiddenException(
                code="forbidden",
                message=f"Access denied for role '{current_user.role}'. Required: {[r.value for r in roles]}"
            )
        return current_user
    return role_checker


require_admin = require_role(UserRole.ADMIN)
require_teacher = require_role(UserRole.TEACHER)
require_student = require_role(UserRole.STUDENT)
require_teacher_or_admin = require_role(UserRole.TEACHER, UserRole.ADMIN)
