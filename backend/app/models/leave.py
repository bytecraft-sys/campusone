import uuid
from datetime import date
from typing import Optional, List
from sqlalchemy import (
    Column, Date, ForeignKey, String, Text, Enum as SQLEnum
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base, TimestampMixin, GUID, OptimisticLockMixin
from app.models.enums import LeaveStatus


class LeaveRequest(Base, TimestampMixin, OptimisticLockMixin):
    __tablename__ = "leave_requests"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    student_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("student_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    assigned_teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    from_date: Mapped[date] = mapped_column(Date, nullable=False)
    to_date: Mapped[date] = mapped_column(Date, nullable=False)
    reason: Mapped[str] = mapped_column(Text, nullable=False)
    # Section 9.2: The cached leave_requests.status always equals the latest decision
    status: Mapped[LeaveStatus] = mapped_column(SQLEnum(LeaveStatus), default=LeaveStatus.PENDING, nullable=False, index=True)

    attachments: Mapped[List["LeaveAttachment"]] = relationship(back_populates="leave_request", cascade="all, delete-orphan")
    decisions: Mapped[List["LeaveDecision"]] = relationship(back_populates="leave_request", cascade="all, delete-orphan")


class LeaveAttachment(Base, TimestampMixin):
    __tablename__ = "leave_attachments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    leave_request_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("leave_requests.id", ondelete="CASCADE"), nullable=False)
    file_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("files.id", ondelete="RESTRICT"), nullable=False)

    leave_request: Mapped["LeaveRequest"] = relationship(back_populates="attachments")


class LeaveDecision(Base, TimestampMixin):
    """
    Section 9.2: leave_decisions is append-only.
    Every step writes a leave_decisions row with actor, time, decision, and comments.
    """
    __tablename__ = "leave_decisions"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    leave_request_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("leave_requests.id", ondelete="CASCADE"), nullable=False, index=True)
    actor_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("users.id", ondelete="RESTRICT"), nullable=False)
    decision: Mapped[str] = mapped_column(String(64), nullable=False)  # 'APPROVED', 'REJECTED', 'REASSIGNED', 'CANCELLED', 'ESCALATED'
    comments: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    to_teacher_id: Mapped[Optional[uuid.UUID]] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=True)

    leave_request: Mapped["LeaveRequest"] = relationship(back_populates="decisions")
