import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user, require_admin
from app.db.session import get_db
from app.models.identity import User
from app.schemas.platform import (
    AuditLogResponse,
    DataRequestCreate,
    DataRequestResponse,
    DataRequestUpdate,
    PolicySettingResponse,
    PolicySettingUpdate,
    UserDataExportResponse,
)
from app.services.platform_service import PlatformService

router = APIRouter(tags=["Platform, Policies & Privacy (DPDP)"])


# --- User DPDP Requests ---
@router.post("/me/data-requests", response_model=DataRequestResponse, status_code=status.HTTP_201_CREATED)
async def submit_my_data_request(
    payload: DataRequestCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Submit a Data Subject Request (Export, Correction, Erasure) under India's DPDP Act 2023."""
    service = PlatformService(db)
    return await service.submit_data_request(current_user, payload)


@router.get("/me/data-requests", response_model=List[DataRequestResponse])
async def list_my_data_requests(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = PlatformService(db)
    return await service.list_user_data_requests(current_user)


@router.get("/me/data-export", response_model=UserDataExportResponse)
async def export_my_data(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Download comprehensive personal data and academic activity archive."""
    service = PlatformService(db)
    return await service.export_user_data(current_user)


# --- Admin DPDP Supervision ---
@router.get("/admin/data-requests", response_model=List[DataRequestResponse])
async def list_all_data_requests(
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = PlatformService(db)
    return await service.list_all_data_requests()


@router.patch("/admin/data-requests/{request_id}", response_model=DataRequestResponse)
async def process_data_request(
    request_id: uuid.UUID,
    payload: DataRequestUpdate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Process DPDP request. Erasure requests automatically anonymize personal data."""
    service = PlatformService(db)
    return await service.process_data_request(current_user, request_id, payload)


# --- Policies & Settings ---
@router.get("/admin/policies", response_model=List[PolicySettingResponse])
async def get_all_policies(
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = PlatformService(db)
    return await service.get_all_policies()


@router.put("/admin/policies/{key}", response_model=PolicySettingResponse)
async def update_policy(
    key: str,
    payload: PolicySettingUpdate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Update institutional policy setting. Old values and actor are saved to append-only audit log."""
    service = PlatformService(db)
    return await service.update_policy(current_user, key, payload)


# --- Audit Logs Explorer ---
@router.get("/admin/audit-logs", response_model=List[AuditLogResponse])
async def query_audit_logs(
    actor_id: Optional[uuid.UUID] = Query(None),
    action: Optional[str] = Query(None),
    resource_type: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=100),
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Search append-only audit trail filterable by actor, action, and resource type."""
    service = PlatformService(db)
    return await service.query_audit_logs(
        actor_id=actor_id,
        action=action,
        resource_type=resource_type,
        limit=limit,
    )

