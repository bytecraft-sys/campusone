import uuid
from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import AssignmentStatus, SubmissionStatus, FileScanStatus


class FileResponse(BaseModel):
    id: uuid.UUID
    file_name: str
    file_size: int
    content_type: str
    scan_status: FileScanStatus
    model_config = ConfigDict(from_attributes=True)


class AssignmentCreate(BaseModel):
    section_id: uuid.UUID
    subject_id: uuid.UUID
    title: str = Field(..., min_length=3, examples=["Data Structures Lab 1"])
    instructions: str = Field(..., examples=["Implement AVL trees in C++"])
    due_at: datetime
    max_marks: float = Field(..., gt=0, examples=[20.0])
    allow_late: bool = False
    late_until: Optional[datetime] = None
    late_penalty_percent: float = Field(default=0.0, ge=0, le=100)
    allow_resubmission: bool = True
    attachment_file_ids: List[uuid.UUID] = []
    status: AssignmentStatus = AssignmentStatus.PUBLISHED


class AssignmentResponse(BaseModel):
    id: uuid.UUID
    section_id: uuid.UUID
    subject_id: uuid.UUID
    teacher_id: uuid.UUID
    title: str
    instructions: str
    due_at: datetime
    max_marks: float
    allow_late: bool
    late_until: Optional[datetime]
    late_penalty_percent: float
    allow_resubmission: bool
    status: AssignmentStatus
    created_at: datetime
    model_config = ConfigDict(from_attributes=True)


class SubmissionCreate(BaseModel):
    file_ids: List[uuid.UUID] = []
    text_answer: Optional[str] = None


class SubmissionResponse(BaseModel):
    id: uuid.UUID
    assignment_id: uuid.UUID
    student_id: uuid.UUID
    version_no: int
    is_current: bool
    text_answer: Optional[str]
    status: SubmissionStatus
    marks: Optional[float]
    feedback: Optional[str]
    submitted_at: datetime
    is_late: bool
    model_config = ConfigDict(from_attributes=True)


class GradeSubmissionRequest(BaseModel):
    marks: float = Field(..., ge=0)
    feedback: Optional[str] = None
    status: SubmissionStatus = SubmissionStatus.GRADED


class GrantExtensionRequest(BaseModel):
    student_id: uuid.UUID
    extended_due_at: datetime
    reason: Optional[str] = None

