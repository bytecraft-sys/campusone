import uuid
from datetime import date, datetime
from typing import Optional, List
from sqlalchemy import (
    Boolean, Column, Date, DateTime, ForeignKey, Integer, String,
    UniqueConstraint, Enum as SQLEnum, CheckConstraint
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base, TimestampMixin, GUID, OptimisticLockMixin
from app.models.enums import AttendanceStatus, SessionStatus, CorrectionStatus


class AttendanceSession(Base, TimestampMixin, OptimisticLockMixin):
    __tablename__ = "attendance_sessions"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False, index=True)
    # subject_id is nullable for DAILY attendance, mandatory for SUBJECT attendance
    subject_id: Mapped[Optional[uuid.UUID]] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=True, index=True)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    session_date: Mapped[date] = mapped_column(Date, nullable=False, index=True)
    sequence_no: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    status: Mapped[SessionStatus] = mapped_column(SQLEnum(SessionStatus), default=SessionStatus.OPEN, nullable=False)
    finalized_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)

    records: Mapped[List["AttendanceRecord"]] = relationship(back_populates="session", cascade="all, delete-orphan")

    __table_args__ = (
        UniqueConstraint("section_id", "subject_id", "session_date", "sequence_no", name="uq_attendance_session_unique"),
    )


class AttendanceRecord(Base, TimestampMixin):
    __tablename__ = "attendance_records"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    session_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("attendance_sessions.id", ondelete="CASCADE"), nullable=False, index=True)
    student_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("student_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    status: Mapped[AttendanceStatus] = mapped_column(SQLEnum(AttendanceStatus), default=AttendanceStatus.NOT_MARKED, nullable=False)

    session: Mapped["AttendanceSession"] = relationship(back_populates="records")
    corrections: Mapped[List["AttendanceCorrection"]] = relationship(back_populates="record", cascade="all, delete-orphan")

    __table_args__ = (
        # Section 9.2: One attendance record per student per session
        UniqueConstraint("session_id", "student_id", name="uq_session_student_attendance"),
    )


class AttendanceCorrection(Base, TimestampMixin):
    __tablename__ = "attendance_corrections"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    record_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("attendance_records.id", ondelete="CASCADE"), nullable=False, index=True)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False)
    previous_status: Mapped[AttendanceStatus] = mapped_column(SQLEnum(AttendanceStatus), nullable=False)
    new_status: Mapped[AttendanceStatus] = mapped_column(SQLEnum(AttendanceStatus), nullable=False)
    reason: Mapped[str] = mapped_column(String(500), nullable=False)
    requires_admin_approval: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    status: Mapped[CorrectionStatus] = mapped_column(SQLEnum(CorrectionStatus), default=CorrectionStatus.PENDING, nullable=False)
    approved_by_id: Mapped[Optional[uuid.UUID]] = mapped_column(GUID(), ForeignKey("users.id", ondelete="SET NULL"), nullable=True)

    record: Mapped["AttendanceRecord"] = relationship(back_populates="corrections")
