import uuid
from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field
from app.models.enums import EnrollmentStatus, UserRole


class DepartmentCreate(BaseModel):
    code: str = Field(..., examples=["CSE"])
    name: str = Field(..., examples=["Computer Science and Engineering"])


class DepartmentResponse(DepartmentCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class AcademicYearCreate(BaseModel):
    name: str = Field(..., examples=["2026-2027"])
    start_date: date
    end_date: date
    is_current: bool = False


class AcademicYearResponse(AcademicYearCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class CourseCreate(BaseModel):
    department_id: uuid.UUID
    code: str = Field(..., examples=["BT-CSE"])
    name: str = Field(..., examples=["B.Tech Computer Science and Engineering"])
    total_semesters: int = Field(default=8, ge=1, le=12)


class CourseResponse(CourseCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class SubjectCreate(BaseModel):
    code: str = Field(..., examples=["CS301"])
    name: str = Field(..., examples=["Database Management Systems"])
    credit_hours: int = Field(default=3, ge=1, le=10)


class SubjectResponse(SubjectCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class CourseSubjectCreate(BaseModel):
    course_id: uuid.UUID
    subject_id: uuid.UUID
    semester_no: int = Field(..., ge=1, le=12)
    is_elective: bool = False


class CourseSubjectResponse(CourseSubjectCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class SectionCreate(BaseModel):
    course_id: uuid.UUID
    academic_year_id: uuid.UUID
    semester_no: int = Field(..., ge=1, le=12)
    name: str = Field(..., examples=["A"])


class SectionResponse(SectionCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class EnrollmentCreate(BaseModel):
    student_id: uuid.UUID
    section_id: uuid.UUID
    academic_year_id: uuid.UUID


class EnrollmentResponse(BaseModel):
    id: uuid.UUID
    student_id: uuid.UUID
    section_id: uuid.UUID
    academic_year_id: uuid.UUID
    status: EnrollmentStatus
    model_config = ConfigDict(from_attributes=True)


class TeachingAssignmentCreate(BaseModel):
    teacher_id: uuid.UUID
    section_id: uuid.UUID
    subject_id: uuid.UUID
    academic_year_id: uuid.UUID


class TeachingAssignmentResponse(TeachingAssignmentCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class TeacherSubstitutionCreate(BaseModel):
    original_teacher_id: uuid.UUID
    substitute_teacher_id: uuid.UUID
    section_id: uuid.UUID
    subject_id: uuid.UUID
    from_date: date
    to_date: date


class TeacherSubstitutionResponse(TeacherSubstitutionCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class TimeSlotCreate(BaseModel):
    day_of_week: int = Field(..., ge=0, le=6, examples=[0])  # 0=Monday, 6=Sunday
    start_time: time
    end_time: time
    label: Optional[str] = Field(None, examples=["Period 1"])


class TimeSlotResponse(TimeSlotCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class TimetableEntryCreate(BaseModel):
    section_id: uuid.UUID
    subject_id: uuid.UUID
    teacher_id: uuid.UUID
    time_slot_id: uuid.UUID


class TimetableEntryResponse(TimetableEntryCreate):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)


class TodayScheduleItem(BaseModel):
    id: uuid.UUID
    subject_name: str
    subject_code: str
    teacher_name: str
    section_name: str
    start_time: time
    end_time: time
    period_label: Optional[str]


class AdminUserCreate(BaseModel):
    full_name: str
    login_id: str
    email: str
    role: UserRole
    department_id: Optional[uuid.UUID] = None  # For teacher
    section_id: Optional[uuid.UUID] = None     # For student enrollment


class AdminUserCreateResponse(BaseModel):
    user_id: uuid.UUID
    login_id: str
    role: UserRole
    activation_token: str
    activation_link: str
    expires_at: str


class CSVRowValidation(BaseModel):
    row_number: int
    full_name: str
    login_id: str
    email: str
    role: str
    is_valid: bool
    error: Optional[str] = None


class CSVImportDryRunReport(BaseModel):
    total_rows: int
    valid_rows: int
    invalid_rows: int
    errors: List[CSVRowValidation]
