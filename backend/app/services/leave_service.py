import uuid
from datetime import date, datetime, timezone
from typing import List, Optional
from sqlalchemy import select, and_, or_
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.exceptions import (
    CampusOneException,
    ConflictException,
    ForbiddenException,
    NotFoundException,
)
from app.models.enums import EnrollmentStatus, LeaveStatus, UserRole
from app.models.academic import (
    Enrollment,
    Section,
    StudentProfile,
    TeacherProfile,
    TeachingAssignment,
)
from app.models.identity import User
from app.models.leave import LeaveAttachment, LeaveDecision, LeaveRequest
from app.schemas.leave import (
    LeaveApplyRequest,
    LeaveDecisionRequest,
    LeaveReassignRequest,
)


class LeaveService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_student_profile(self, user: User) -> StudentProfile:
        res = await self.db.execute(
            select(StudentProfile).where(StudentProfile.user_id == user.id)
        )
        student = res.scalar_one_or_none()
        if not student:
            raise ForbiddenException(code="not_student", message="User does not have a student profile.")
        return student

    async def get_teacher_profile(self, user: User) -> TeacherProfile:
        res = await self.db.execute(
            select(TeacherProfile).where(TeacherProfile.user_id == user.id)
        )
        teacher = res.scalar_one_or_none()
        if not teacher:
            raise ForbiddenException(code="not_teacher", message="User does not have a teacher profile.")
        return teacher

    async def apply_leave(self, user: User, payload: LeaveApplyRequest) -> LeaveRequest:
        student = await self.get_student_profile(user)

        if payload.from_date > payload.to_date:
            raise CampusOneException(
                code="invalid_date_range",
                message="Start date must be on or before end date.",
                status_code=400
            )

        # 1. Check overlapping active requests (status PENDING or APPROVED)
        overlap_query = select(LeaveRequest).where(
            LeaveRequest.student_id == student.id,
            LeaveRequest.status.in_([LeaveStatus.PENDING, LeaveStatus.APPROVED]),
            or_(
                and_(LeaveRequest.from_date <= payload.from_date, LeaveRequest.to_date >= payload.from_date),
                and_(LeaveRequest.from_date <= payload.to_date, LeaveRequest.to_date >= payload.to_date),
                and_(LeaveRequest.from_date >= payload.from_date, LeaveRequest.to_date <= payload.to_date),
            )
        )
        overlap = await self.db.execute(overlap_query)
        if overlap.scalar_one_or_none():
            raise ConflictException(
                code="overlapping_leave",
                message="You already have an active or approved leave application for these dates."
            )

        # 2. Determine assigned teacher (from active section enrollment teaching assignments)
        enr_query = (
            select(Enrollment)
            .where(
                Enrollment.student_id == student.id,
                Enrollment.status == EnrollmentStatus.ACTIVE
            )
        )
        enr = (await self.db.execute(enr_query)).scalar_one_or_none()
        if not enr:
            raise CampusOneException(
                code="no_active_enrollment",
                message="Student has no active enrollment in this academic year.",
                status_code=400
            )

        # Look up assigned class teacher or first teaching assignment
        assign_query = select(TeachingAssignment).where(TeachingAssignment.section_id == enr.section_id)
        assign = (await self.db.execute(assign_query)).scalars().first()
        if not assign:
            raise CampusOneException(
                code="no_assigned_teacher",
                message="No teacher is currently assigned to your section.",
                status_code=400
            )

        assigned_teacher_id = assign.teacher_id

        # 3. Create LeaveRequest
        leave_req = LeaveRequest(
            student_id=student.id,
            assigned_teacher_id=assigned_teacher_id,
            from_date=payload.from_date,
            to_date=payload.to_date,
            reason=payload.reason,
            status=LeaveStatus.PENDING,
        )
        self.db.add(leave_req)
        await self.db.flush()

        for fid in payload.attachment_file_ids:
            self.db.add(LeaveAttachment(leave_request_id=leave_req.id, file_id=fid))

        # Record initial PENDING decision step in append-only table
        self.db.add(LeaveDecision(
            leave_request_id=leave_req.id,
            actor_id=user.id,
            decision="APPLIED",
            comments="Leave submitted by student",
        ))

        await self.db.commit()

        # Reload with relationships
        res = await self.db.execute(
            select(LeaveRequest)
            .where(LeaveRequest.id == leave_req.id)
            .options(selectinload(LeaveRequest.decisions))
        )
        return res.scalar_one()

    async def decide_leave(
        self, user: User, leave_id: uuid.UUID, payload: LeaveDecisionRequest
    ) -> LeaveRequest:
        teacher = await self.get_teacher_profile(user)

        res = await self.db.execute(
            select(LeaveRequest)
            .where(LeaveRequest.id == leave_id)
            .options(selectinload(LeaveRequest.decisions))
        )
        leave_req = res.scalar_one_or_none()
        if not leave_req:
            raise NotFoundException(code="leave_not_found", message="Leave request not found.")

        # Section 8.4: Other teachers get 403 not_assigned_teacher
        if leave_req.assigned_teacher_id != teacher.id:
            raise ForbiddenException(
                code="not_assigned_teacher",
                message="Only the assigned teacher can decide this request."
            )

        # Section 8.4: A decided request returns 409 already_decided
        if leave_req.status in [LeaveStatus.APPROVED, LeaveStatus.REJECTED, LeaveStatus.CANCELLED]:
            raise ConflictException(
                code="already_decided",
                message=f"This leave request has already been marked {leave_req.status.value}."
            )

        new_status = LeaveStatus.APPROVED if payload.decision == "APPROVED" else LeaveStatus.REJECTED
        leave_req.status = new_status

        # Section 9.2: leave_decisions is append-only
        decision_log = LeaveDecision(
            leave_request_id=leave_req.id,
            actor_id=user.id,
            decision=payload.decision,
            comments=payload.comments,
        )
        self.db.add(decision_log)
        await self.db.commit()

        await self.db.refresh(leave_req)
        return leave_req

    async def reassign_leave(
        self, user: User, leave_id: uuid.UUID, payload: LeaveReassignRequest
    ) -> LeaveRequest:
        res = await self.db.execute(
            select(LeaveRequest)
            .where(LeaveRequest.id == leave_id)
            .options(selectinload(LeaveRequest.decisions))
        )
        leave_req = res.scalar_one_or_none()
        if not leave_req:
            raise NotFoundException(code="leave_not_found", message="Leave request not found.")

        # Reassignment allowed by currently assigned teacher or admin
        if user.role == UserRole.TEACHER:
            teacher = await self.get_teacher_profile(user)
            if leave_req.assigned_teacher_id != teacher.id:
                raise ForbiddenException(
                    code="not_authorized_to_reassign",
                    message="You can only reassign requests assigned to you."
                )
        elif user.role != UserRole.ADMIN:
            raise ForbiddenException(code="forbidden", message="Only teacher or admin can reassign leave.")

        # Verify target teacher exists
        target_teacher_res = await self.db.execute(
            select(TeacherProfile).where(TeacherProfile.id == payload.to_teacher_id)
        )
        if not target_teacher_res.scalar_one_or_none():
            raise NotFoundException(code="target_teacher_not_found", message="Target teacher not found.")

        leave_req.assigned_teacher_id = payload.to_teacher_id

        decision_log = LeaveDecision(
            leave_request_id=leave_req.id,
            actor_id=user.id,
            decision="REASSIGNED",
            comments=payload.comments,
            to_teacher_id=payload.to_teacher_id,
        )
        self.db.add(decision_log)
        await self.db.commit()

        await self.db.refresh(leave_req)
        return leave_req

    async def cancel_leave(self, user: User, leave_id: uuid.UUID) -> LeaveRequest:
        student = await self.get_student_profile(user)

        res = await self.db.execute(
            select(LeaveRequest)
            .where(LeaveRequest.id == leave_id)
            .options(selectinload(LeaveRequest.decisions))
        )
        leave_req = res.scalar_one_or_none()
        if not leave_req:
            raise NotFoundException(code="leave_not_found", message="Leave request not found.")

        if leave_req.student_id != student.id:
            raise ForbiddenException(code="not_your_leave", message="You can only cancel your own leave requests.")

        if leave_req.status != LeaveStatus.PENDING:
            raise ConflictException(
                code="cannot_cancel",
                message=f"Leave requests in status '{leave_req.status.value}' cannot be cancelled."
            )

        leave_req.status = LeaveStatus.CANCELLED

        self.db.add(LeaveDecision(
            leave_request_id=leave_req.id,
            actor_id=user.id,
            decision="CANCELLED",
            comments="Cancelled by student",
        ))
        await self.db.commit()

        await self.db.refresh(leave_req)
        return leave_req

