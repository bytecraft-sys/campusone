import uuid
from datetime import datetime
from typing import Optional, List
from sqlalchemy import (
    Boolean, Column, DateTime, Float, ForeignKey, Integer, String,
    Text, UniqueConstraint, Enum as SQLEnum, Index
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base, TimestampMixin, GUID, OptimisticLockMixin
from app.models.enums import AssignmentStatus, SubmissionStatus, FileScanStatus


class File(Base, TimestampMixin):
    __tablename__ = "files"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    file_name: Mapped[str] = mapped_column(String(255), nullable=False)
    file_path: Mapped[str] = mapped_column(String(512), nullable=False)
    file_size: Mapped[int] = mapped_column(Integer, nullable=False)
    content_type: Mapped[str] = mapped_column(String(128), nullable=False)
    scan_status: Mapped[FileScanStatus] = mapped_column(SQLEnum(FileScanStatus), default=FileScanStatus.PENDING, nullable=False)


class Assignment(Base, TimestampMixin, OptimisticLockMixin):
    __tablename__ = "assignments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False, index=True)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=False, index=True)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)

    title: Mapped[str] = mapped_column(String(255), nullable=False)
    instructions: Mapped[str] = mapped_column(Text, nullable=False)
    due_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    max_marks: Mapped[float] = mapped_column(Float, nullable=False)

    allow_late: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    late_until: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    late_penalty_percent: Mapped[float] = mapped_column(Float, default=0.0, nullable=False)
    allow_resubmission: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    status: Mapped[AssignmentStatus] = mapped_column(SQLEnum(AssignmentStatus), default=AssignmentStatus.DRAFT, nullable=False)

    attachments: Mapped[List["AssignmentAttachment"]] = relationship(back_populates="assignment", cascade="all, delete-orphan")
    submissions: Mapped[List["Submission"]] = relationship(back_populates="assignment", cascade="all, delete-orphan")
    extensions: Mapped[List["AssignmentExtension"]] = relationship(back_populates="assignment", cascade="all, delete-orphan")


class AssignmentAttachment(Base, TimestampMixin):
    __tablename__ = "assignment_attachments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    assignment_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("assignments.id", ondelete="CASCADE"), nullable=False)
    file_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("files.id", ondelete="RESTRICT"), nullable=False)

    assignment: Mapped["Assignment"] = relationship(back_populates="attachments")
    file: Mapped["File"] = relationship()


class AssignmentExtension(Base, TimestampMixin):
    __tablename__ = "assignment_extensions"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    assignment_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("assignments.id", ondelete="CASCADE"), nullable=False, index=True)
    student_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("student_profiles.id", ondelete="CASCADE"), nullable=False, index=True)
    extended_due_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    reason: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)

    assignment: Mapped["Assignment"] = relationship(back_populates="extensions")

    __table_args__ = (
        UniqueConstraint("assignment_id", "student_id", name="uq_assignment_student_extension"),
    )


class Submission(Base, TimestampMixin, OptimisticLockMixin):
    __tablename__ = "submissions"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    assignment_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("assignments.id", ondelete="RESTRICT"), nullable=False, index=True)
    student_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("student_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    version_no: Mapped[int] = mapped_column(Integer, default=1, nullable=False)
    is_current: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    text_answer: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    status: Mapped[SubmissionStatus] = mapped_column(SQLEnum(SubmissionStatus), default=SubmissionStatus.SUBMITTED, nullable=False)
    marks: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    feedback: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    submitted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_late: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    assignment: Mapped["Assignment"] = relationship(back_populates="submissions")
    submission_files: Mapped[List["SubmissionFile"]] = relationship(back_populates="submission", cascade="all, delete-orphan")

    __table_args__ = (
        # Section 9.2: One current submission version per student per assignment
        Index(
            "uq_current_submission_per_student",
            "assignment_id",
            "student_id",
            unique=True,
            postgresql_where=(is_current == True),
            sqlite_where=(is_current == True),
        ),
    )



class SubmissionFile(Base, TimestampMixin):
    __tablename__ = "submission_files"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    submission_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("submissions.id", ondelete="CASCADE"), nullable=False)
    file_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("files.id", ondelete="RESTRICT"), nullable=False)

    submission: Mapped["Submission"] = relationship(back_populates="submission_files")
    file: Mapped["File"] = relationship()


class StudyMaterial(Base, TimestampMixin):
    __tablename__ = "study_materials"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False, index=True)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=False)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    file_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("files.id", ondelete="RESTRICT"), nullable=False)

    file: Mapped["File"] = relationship()
