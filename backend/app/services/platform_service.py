import uuid
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, List, Optional
from sqlalchemy import select, and_, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import (
    CampusOneException,
    NotFoundException,
    ForbiddenException,
)
from app.models.enums import DataRequestStatus, DataRequestType, UserRole
from app.models.identity import DataRequest, User
from app.models.academic import Enrollment, StudentProfile
from app.models.attendance import AttendanceRecord
from app.models.assignments import Submission
from app.models.leave import LeaveRequest
from app.models.platform import AuditLog, PolicySetting
from app.schemas.platform import (
    DataRequestCreate,
    DataRequestUpdate,
    PolicySettingUpdate,
    UserDataExportResponse,
)


class PlatformService:
    def __init__(self, db: AsyncSession):
        self.db = db

    # --- DPDP Data Requests ---
    async def submit_data_request(self, user: User, payload: DataRequestCreate) -> DataRequest:
        now = datetime.now(timezone.utc)
        # DPDP compliance: statutory 30-day resolution due date
        due_date = now + timedelta(days=30)

        req = DataRequest(
            user_id=user.id,
            request_type=payload.request_type,
            status=DataRequestStatus.PENDING,
            details=payload.details,
            due_date=due_date,
        )
        self.db.add(req)
        await self.db.commit()
        await self.db.refresh(req)
        return req

    async def list_user_data_requests(self, user: User) -> List[DataRequest]:
        res = await self.db.execute(
            select(DataRequest)
            .where(DataRequest.user_id == user.id)
            .order_by(DataRequest.created_at.desc())
        )
        return res.scalars().all()

    async def list_all_data_requests(self) -> List[DataRequest]:
        res = await self.db.execute(
            select(DataRequest).order_by(DataRequest.due_date.asc())
        )
        return res.scalars().all()

    async def process_data_request(
        self, admin_user: User, request_id: uuid.UUID, payload: DataRequestUpdate
    ) -> DataRequest:
        res = await self.db.execute(select(DataRequest).where(DataRequest.id == request_id))
        req = res.scalar_one_or_none()
        if not req:
            raise NotFoundException(code="data_request_not_found", message="Data request not found.")

        req.status = payload.status
        if payload.details:
            req.details = payload.details

        # Section 3.3: Erasure anonymizes records that must be kept
        if payload.status == DataRequestStatus.COMPLETED and req.request_type == DataRequestType.ERASURE:
            target_user_res = await self.db.execute(select(User).where(User.id == req.user_id))
            target_user = target_user_res.scalar_one_or_none()
            if target_user:
                target_user.full_name = f"Anonymized User {str(target_user.id)[:8]}"
                target_user.email = f"anonymized_{str(target_user.id)[:8]}@campusone.local"
                target_user.is_active = False

        # Audit event
        self.db.add(AuditLog(
            actor_id=admin_user.id,
            action="data_request.process",
            resource_type="data_request",
            resource_id=str(req.id),
            new_value=f"Status: {payload.status.value}",
        ))

        await self.db.commit()
        await self.db.refresh(req)
        return req

    async def export_user_data(self, user: User) -> UserDataExportResponse:
        """Section 10.1: Exports all personal data and academic records under DPDP Act."""
        # 1. Fetch Student profile if applicable
        profile_data = None
        enrollment_list = []
        att_list = []
        sub_list = []
        leave_list = []

        if user.role == UserRole.STUDENT:
            stud_res = await self.db.execute(select(StudentProfile).where(StudentProfile.user_id == user.id))
            student = stud_res.scalar_one_or_none()
            if student:
                profile_data = {
                    "admission_number": student.admission_number,
                    "date_of_birth": str(student.date_of_birth),
                    "guardian_name": student.guardian_name,
                    "guardian_phone": student.guardian_phone,
                }

                # Enrollments
                enr_res = await self.db.execute(select(Enrollment).where(Enrollment.student_id == student.id))
                for e in enr_res.scalars().all():
                    enrollment_list.append({"section_id": str(e.section_id), "academic_year_id": str(e.academic_year_id), "status": e.status.value})

                # Attendance
                att_res = await self.db.execute(select(AttendanceRecord).where(AttendanceRecord.student_id == student.id))
                for a in att_res.scalars().all():
                    att_list.append({"session_id": str(a.session_id), "status": a.status.value, "created_at": a.created_at.isoformat()})

                # Submissions
                sub_res = await self.db.execute(select(Submission).where(Submission.student_id == student.id))
                for s in sub_res.scalars().all():
                    sub_list.append({"assignment_id": str(s.assignment_id), "version_no": s.version_no, "marks": s.marks, "status": s.status.value, "submitted_at": s.submitted_at.isoformat()})

                # Leave
                leave_res = await self.db.execute(select(LeaveRequest).where(LeaveRequest.student_id == student.id))
                for l in leave_res.scalars().all():
                    leave_list.append({"from_date": str(l.from_date), "to_date": str(l.to_date), "reason": l.reason, "status": l.status.value})

        return UserDataExportResponse(
            user_id=user.id,
            login_id=user.login_id,
            email=user.email,
            full_name=user.full_name,
            role=user.role.value,
            created_at=user.created_at,
            profile=profile_data,
            enrollments=enrollment_list,
            attendance_records=att_list,
            submissions=sub_list,
            leave_requests=leave_list,
        )

    # --- Policies & Settings ---
    async def get_all_policies(self) -> List[PolicySetting]:
        res = await self.db.execute(select(PolicySetting).order_by(PolicySetting.key.asc()))
        return res.scalars().all()

    async def update_policy(self, admin_user: User, key: str, payload: PolicySettingUpdate) -> PolicySetting:
        res = await self.db.execute(select(PolicySetting).where(PolicySetting.key == key))
        policy = res.scalar_one_or_none()
        if not policy:
            raise NotFoundException(code="policy_not_found", message=f"Policy with key '{key}' does not exist.")

        old_val = policy.value
        policy.old_value = old_val
        policy.value = payload.value

        # Section 3.3 & Page 18: Every change is saved in the audit log with who changed it and old value
        self.db.add(AuditLog(
            actor_id=admin_user.id,
            action="policy.update",
            resource_type="policy_setting",
            resource_id=policy.key,
            old_value=old_val,
            new_value=payload.value,
        ))

        await self.db.commit()
        await self.db.refresh(policy)
        return policy

    # --- Audit Logs Explorer ---
    async def query_audit_logs(
        self,
        actor_id: Optional[uuid.UUID] = None,
        action: Optional[str] = None,
        resource_type: Optional[str] = None,
        limit: int = 50,
    ) -> List[AuditLog]:
        query = select(AuditLog)
        if actor_id:
            query = query.where(AuditLog.actor_id == actor_id)
        if action:
            query = query.where(AuditLog.action == action)
        if resource_type:
            query = query.where(AuditLog.resource_type == resource_type)

        query = query.order_by(AuditLog.created_at.desc()).limit(min(limit, 100))
        res = await self.db.execute(query)
        return res.scalars().all()

