import uuid
from fastapi import APIRouter, Depends, Header, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import (
    get_current_user,
    require_student,
    require_teacher,
    require_teacher_or_admin,
)
from app.db.session import get_db
from app.models.identity import User
from app.schemas.leave import (
    LeaveApplyRequest,
    LeaveDecisionRequest,
    LeaveReassignRequest,
    LeaveResponse,
)
from app.services.leave_service import LeaveService

router = APIRouter(prefix="/leaves", tags=["Leave Management"])


@router.post("", response_model=LeaveResponse, status_code=status.HTTP_201_CREATED)
async def apply_for_leave(
    payload: LeaveApplyRequest,
    idempotency_key: str = Header(None, alias="Idempotency-Key"),
    current_user: User = Depends(require_student),
    db: AsyncSession = Depends(get_db),
):
    """
    Apply for student leave. Overlapping active requests return 409 Conflict.
    Auto-routes to the designated class teacher.
    """
    service = LeaveService(db)
    return await service.apply_leave(current_user, payload)


@router.patch("/{leave_id}/decision", response_model=LeaveResponse)
async def decide_leave_request(
    leave_id: uuid.UUID,
    payload: LeaveDecisionRequest,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """
    Decide leave request (APPROVED or REJECTED).
    Only the assigned teacher can decide (403 for others).
    Already decided requests return 409 already_decided.
    """
    service = LeaveService(db)
    return await service.decide_leave(current_user, leave_id, payload)


@router.post("/{leave_id}/reassign", response_model=LeaveResponse)
async def reassign_leave_request(
    leave_id: uuid.UUID,
    payload: LeaveReassignRequest,
    current_user: User = Depends(require_teacher_or_admin),
    db: AsyncSession = Depends(get_db),
):
    """
    Reassign a leave request to another teacher colleague.
    Allowed by the assigned teacher or an administrator.
    """
    service = LeaveService(db)
    return await service.reassign_leave(current_user, leave_id, payload)


@router.post("/{leave_id}/cancel", response_model=LeaveResponse)
async def cancel_leave_request(
    leave_id: uuid.UUID,
    current_user: User = Depends(require_student),
    db: AsyncSession = Depends(get_db),
):
    """
    Cancel a pending leave request. Allowed only for the owning student while in PENDING status.
    """
    service = LeaveService(db)
    return await service.cancel_leave(current_user, leave_id)

