import uuid
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import UserRole


class DevicePayload(BaseModel):
    platform: str = Field(..., examples=["android"])  # 'android', 'ios', 'web'
    name: str = Field(..., examples=["Pixel 8 Pro"])


class LoginRequest(BaseModel):
    login_id: str = Field(..., examples=["STU2026001"])
    password: str = Field(..., examples=["Secur3P@ssw0rd"])
    device: DevicePayload


class UserResponse(BaseModel):
    id: uuid.UUID
    login_id: str
    email: str
    full_name: str
    role: UserRole
    must_change_password: bool

    model_config = ConfigDict(from_attributes=True)



class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int = 900  # 15 minutes in seconds
    user: UserResponse


class RefreshTokenRequest(BaseModel):
    refresh_token: str
    device: Optional[DevicePayload] = None


class ActivationRequest(BaseModel):
    token: str
    new_password: str
    confirm_password: str
    guardian_consent: bool = False
    guardian_name: Optional[str] = None


class ChangePasswordRequest(BaseModel):
    current_password: str
    new_password: str
    confirm_password: str


class PasswordResetRequest(BaseModel):
    email: str


class PasswordResetConfirm(BaseModel):
    token: str
    new_password: str
    confirm_password: str


class MFAEnrollResponse(BaseModel):
    secret: str
    qr_code_uri: str


class MFAVerifyRequest(BaseModel):
    code: str
