import uuid
from datetime import date, datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import LeaveStatus


class LeaveApplyRequest(BaseModel):
    from_date: date
    to_date: date
    reason: str = Field(..., min_length=5, examples=["Medical leave for viral fever"])
    attachment_file_ids: List[uuid.UUID] = []


class LeaveDecisionItem(BaseModel):
    id: uuid.UUID
    actor_id: uuid.UUID
    decision: str
    comments: Optional[str]
    to_teacher_id: Optional[uuid.UUID]
    created_at: datetime
    model_config = ConfigDict(from_attributes=True)


class LeaveResponse(BaseModel):
    id: uuid.UUID
    student_id: uuid.UUID
    assigned_teacher_id: uuid.UUID
    from_date: date
    to_date: date
    reason: str
    status: LeaveStatus
    created_at: datetime
    decisions: List[LeaveDecisionItem] = []
    model_config = ConfigDict(from_attributes=True)


class LeaveDecisionRequest(BaseModel):
    decision: str = Field(..., pattern="^(APPROVED|REJECTED)$")
    comments: Optional[str] = None


class LeaveReassignRequest(BaseModel):
    to_teacher_id: uuid.UUID
    comments: Optional[str] = None

