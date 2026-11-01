import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import DataRequestStatus, DataRequestType


class DataRequestCreate(BaseModel):
    request_type: DataRequestType
    details: Optional[str] = Field(None, examples=["Requesting copy of all attendance and assignment submissions under DPDP Act 2023"])


class DataRequestResponse(BaseModel):
    id: uuid.UUID
    user_id: uuid.UUID
    request_type: DataRequestType
    status: DataRequestStatus
    details: Optional[str]
    due_date: datetime
    created_at: datetime
    model_config = ConfigDict(from_attributes=True)


class DataRequestUpdate(BaseModel):
    status: DataRequestStatus
    details: Optional[str] = None


class PolicySettingResponse(BaseModel):
    id: uuid.UUID
    key: str
    value: str
    description: Optional[str]
    old_value: Optional[str]
    updated_at: datetime
    model_config = ConfigDict(from_attributes=True)


class PolicySettingUpdate(BaseModel):
    value: str = Field(..., examples=["80"])


class AuditLogResponse(BaseModel):
    id: uuid.UUID
    actor_id: Optional[uuid.UUID]
    action: str
    resource_type: str
    resource_id: Optional[str]
    ip_address: Optional[str]
    request_id: Optional[str]
    old_value: Optional[str]
    new_value: Optional[str]
    created_at: datetime
    model_config = ConfigDict(from_attributes=True)


class UserDataExportResponse(BaseModel):
    user_id: uuid.UUID
    login_id: str
    email: str
    full_name: str
    role: str
    created_at: datetime
    profile: Optional[Dict[str, Any]] = None
    enrollments: List[Dict[str, Any]] = []
    attendance_records: List[Dict[str, Any]] = []
    submissions: List[Dict[str, Any]] = []
    leave_requests: List[Dict[str, Any]] = []

