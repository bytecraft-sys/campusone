import uuid
from datetime import date, datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import AttendanceStatus, SessionStatus, CorrectionStatus


class RecordStatusItem(BaseModel):
    student_id: uuid.UUID
    status: AttendanceStatus


class OpenSessionRequest(BaseModel):
    section_id: uuid.UUID
    subject_id: Optional[uuid.UUID] = None
    session_date: date
    sequence_no: int = Field(default=1, ge=1)


class AttendanceRecordResponse(BaseModel):
    id: uuid.UUID
    student_id: uuid.UUID
    status: AttendanceStatus
    model_config = ConfigDict(from_attributes=True)


class AttendanceSessionResponse(BaseModel):
    id: uuid.UUID
    section_id: uuid.UUID
    subject_id: Optional[uuid.UUID]
    teacher_id: uuid.UUID
    session_date: date
    sequence_no: int
    status: SessionStatus
    finalized_at: Optional[datetime]
    records: List[AttendanceRecordResponse] = []
    model_config = ConfigDict(from_attributes=True)


class SaveRecordsRequest(BaseModel):
    records: List[RecordStatusItem]


class SaveRecordsResponse(BaseModel):
    saved: int
    not_marked: int


class FinalizeSessionResponse(BaseModel):
    status: SessionStatus
    finalized_at: datetime


class CorrectionRequest(BaseModel):
    new_status: AttendanceStatus
    reason: str = Field(..., min_length=5, examples=["Doctor's certificate verified"])


class CorrectionResponse(BaseModel):
    id: uuid.UUID
    requires_admin_approval: bool
    status: CorrectionStatus
    model_config = ConfigDict(from_attributes=True)


class SubjectAttendanceSummary(BaseModel):
    subject_id: Optional[uuid.UUID]
    subject_name: Optional[str]
    present_count: int
    absent_count: int
    percentage: Optional[float]
    is_low: bool


class StudentAttendanceSummaryResponse(BaseModel):
    overall_percentage: Optional[float]
    is_low: bool
    display_text: str
    total_present: int
    total_absent: int
    subjects: List[SubjectAttendanceSummary] = []

