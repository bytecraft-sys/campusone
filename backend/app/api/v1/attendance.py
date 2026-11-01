import uuid
from fastapi import APIRouter, Depends, Header, Request, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user, require_teacher, require_student
from app.db.session import get_db
from app.models.identity import User
from app.schemas.attendance import (
    AttendanceSessionResponse,
    CorrectionRequest,
    CorrectionResponse,
    FinalizeSessionResponse,
    OpenSessionRequest,
    SaveRecordsRequest,
    SaveRecordsResponse,
    StudentAttendanceSummaryResponse,
)
from app.services.attendance_service import AttendanceService

router = APIRouter(prefix="/attendance", tags=["Attendance"])


@router.post("/sessions", response_model=AttendanceSessionResponse, status_code=status.HTTP_201_CREATED)
async def open_attendance_session(
    payload: OpenSessionRequest,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """
    Open an attendance session for an allocated section and subject.
    Initializes one record per enrolled student set to NOT_MARKED.
    """
    service = AttendanceService(db)
    return await service.open_session(current_user, payload)


@router.put("/sessions/{session_id}/records", response_model=SaveRecordsResponse)
async def save_attendance_records(
    session_id: uuid.UUID,
    payload: SaveRecordsRequest,
    idempotency_key: str = Header(None, alias="Idempotency-Key"),
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """
    Save student attendance records (draft mode).
    Idempotent: students not in section are rejected with 400.
    """
    service = AttendanceService(db)
    return await service.save_records(current_user, session_id, payload)


@router.post("/sessions/{session_id}/finalize", response_model=FinalizeSessionResponse)
async def finalize_attendance_session(
    session_id: uuid.UUID,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """
    Finalize an attendance session. Once finalized, sessions count towards percentages.
    """
    service = AttendanceService(db)
    return await service.finalize_session(current_user, session_id)


@router.post("/records/{record_id}/corrections", response_model=CorrectionResponse, status_code=status.HTTP_201_CREATED)
async def correct_attendance_record(
    record_id: uuid.UUID,
    payload: CorrectionRequest,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """
    Submit a correction with reason. Corrections within window are applied immediately;
    corrections after window flag requires_admin_approval=true.
    """
    service = AttendanceService(db)
    return await service.correct_record(current_user, record_id, payload)


@router.get("/summary", response_model=StudentAttendanceSummaryResponse)
async def get_my_attendance_summary(
    current_user: User = Depends(require_student),
    db: AsyncSession = Depends(get_db),
):
    """
    Retrieve attendance summary for the authenticated student.
    Excludes unfinalized sessions and NOT_MARKED sessions.
    """
    service = AttendanceService(db)
    return await service.get_student_summary(current_user)

