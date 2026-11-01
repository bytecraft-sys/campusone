from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.session import get_db
from app.models.identity import User
from app.schemas.auth import (
    ActivationRequest,
    LoginRequest,
    RefreshTokenRequest,
    TokenResponse,
    UserResponse,
)
from app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/login", response_model=TokenResponse)
async def login(
    payload: LoginRequest,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Authenticate user with institutional login ID and password.
    Returns short-lived access token and rotating refresh token.
    """
    client_ip = request.client.host if request.client else None
    service = AuthService(db)
    return await service.authenticate_user(payload, client_ip=client_ip)


@router.post("/activate", status_code=status.HTTP_200_OK)
async def activate_account(
    payload: ActivationRequest,
    request: Request,
    db: AsyncSession = Depends(get_db)
):
    """
    Activate new account with single-use 72-hour token and set Argon2id password.
    Records privacy and guardian consent for minors.
    """
    client_ip = request.client.host if request.client else None
    service = AuthService(db)
    success = await service.activate_account(payload, client_ip=client_ip)
    return {"status": "success", "message": "Account activated successfully."}


@router.get("/me", response_model=UserResponse)
async def get_me(current_user: User = Depends(get_current_user)):
    """Retrieve currently authenticated user profile."""
    return current_user
