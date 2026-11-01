import uuid
from typing import List
from fastapi import APIRouter, Depends, Header, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import (
    get_current_user,
    require_student,
    require_teacher,
)
from app.db.session import get_db
from app.models.identity import User
from app.schemas.assignments import (
    AssignmentCreate,
    AssignmentResponse,
    GradeSubmissionRequest,
    GrantExtensionRequest,
    SubmissionCreate,
    SubmissionResponse,
)
from app.services.assignment_service import AssignmentService

router = APIRouter(tags=["Assignments"])


@router.post("/assignments", response_model=AssignmentResponse, status_code=status.HTTP_201_CREATED)
async def create_assignment(
    payload: AssignmentCreate,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """Create a new assignment in draft or published status (teacher only)."""
    service = AssignmentService(db)
    return await service.create_assignment(current_user, payload)


@router.get("/assignments", response_model=List[AssignmentResponse])
async def list_assignments(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    List assignments.
    Students see only PUBLISHED assignments for their enrolled sections.
    Teachers see assignments they created.
    """
    service = AssignmentService(db)
    return await service.list_assignments(current_user)


@router.post("/assignments/{assignment_id}/submissions", response_model=SubmissionResponse, status_code=status.HTTP_201_CREATED)
async def submit_assignment(
    assignment_id: uuid.UUID,
    payload: SubmissionCreate,
    idempotency_key: str = Header(None, alias="Idempotency-Key"),
    current_user: User = Depends(require_student),
    db: AsyncSession = Depends(get_db),
):
    """Submit work for an assignment. Generates new version if resubmitted."""
    service = AssignmentService(db)
    return await service.submit_assignment(current_user, assignment_id, payload)


@router.post("/assignments/{assignment_id}/extensions")
async def grant_extension(
    assignment_id: uuid.UUID,
    payload: GrantExtensionRequest,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """Grant an individual deadline extension to a specific student."""
    service = AssignmentService(db)
    ext = await service.grant_extension(current_user, assignment_id, payload)
    return {"status": "success", "extended_due_at": ext.extended_due_at}


@router.patch("/submissions/{submission_id}/grade", response_model=SubmissionResponse)
async def grade_submission(
    submission_id: uuid.UUID,
    payload: GradeSubmissionRequest,
    current_user: User = Depends(require_teacher),
    db: AsyncSession = Depends(get_db),
):
    """Grade a student submission with marks and feedback (teacher only)."""
    service = AssignmentService(db)
    return await service.grade_submission(current_user, submission_id, payload)

