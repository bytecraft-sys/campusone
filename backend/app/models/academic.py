import uuid
from datetime import date, time
from typing import Optional, List
from sqlalchemy import (
    Boolean, Column, Date, ForeignKey, Integer, String, Time,
    UniqueConstraint, Enum as SQLEnum, Index
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base, TimestampMixin, GUID
from app.models.enums import EnrollmentStatus


class Department(Base, TimestampMixin):
    __tablename__ = "departments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(32), unique=True, index=True, nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)

    courses: Mapped[List["Course"]] = relationship(back_populates="department")
    teachers: Mapped[List["TeacherProfile"]] = relationship(back_populates="department")


class AcademicYear(Base, TimestampMixin):
    __tablename__ = "academic_years"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)  # e.g., '2026-2027'
    start_date: Mapped[date] = mapped_column(Date, nullable=False)
    end_date: Mapped[date] = mapped_column(Date, nullable=False)
    is_current: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    sections: Mapped[List["Section"]] = relationship(back_populates="academic_year")
    enrollments: Mapped[List["Enrollment"]] = relationship(back_populates="academic_year")


class Course(Base, TimestampMixin):
    __tablename__ = "courses"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    department_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("departments.id", ondelete="RESTRICT"), nullable=False, index=True)
    code: Mapped[str] = mapped_column(String(32), unique=True, index=True, nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    total_semesters: Mapped[int] = mapped_column(Integer, default=8, nullable=False)

    department: Mapped["Department"] = relationship(back_populates="courses")
    sections: Mapped[List["Section"]] = relationship(back_populates="course")
    course_subjects: Mapped[List["CourseSubject"]] = relationship(back_populates="course")


class Subject(Base, TimestampMixin):
    __tablename__ = "subjects"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    code: Mapped[str] = mapped_column(String(32), unique=True, index=True, nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    credit_hours: Mapped[int] = mapped_column(Integer, default=3, nullable=False)

    course_subjects: Mapped[List["CourseSubject"]] = relationship(back_populates="subject")


class CourseSubject(Base, TimestampMixin):
    __tablename__ = "course_subjects"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    course_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("courses.id", ondelete="CASCADE"), nullable=False)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="CASCADE"), nullable=False)
    semester_no: Mapped[int] = mapped_column(Integer, nullable=False)
    is_elective: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    course: Mapped["Course"] = relationship(back_populates="course_subjects")
    subject: Mapped["Subject"] = relationship(back_populates="course_subjects")

    __table_args__ = (
        UniqueConstraint("course_id", "subject_id", "semester_no", name="uq_course_subject_semester"),
    )


class Section(Base, TimestampMixin):
    __tablename__ = "sections"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    course_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("courses.id", ondelete="RESTRICT"), nullable=False)
    academic_year_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("academic_years.id", ondelete="RESTRICT"), nullable=False)
    semester_no: Mapped[int] = mapped_column(Integer, nullable=False)
    name: Mapped[str] = mapped_column(String(32), nullable=False)  # 'A', 'B', etc.

    course: Mapped["Course"] = relationship(back_populates="sections")
    academic_year: Mapped["AcademicYear"] = relationship(back_populates="sections")
    enrollments: Mapped[List["Enrollment"]] = relationship(back_populates="section")
    teaching_assignments: Mapped[List["TeachingAssignment"]] = relationship(back_populates="section")

    __table_args__ = (
        UniqueConstraint("course_id", "academic_year_id", "semester_no", "name", name="uq_section_identity"),
    )


class StudentProfile(Base, TimestampMixin):
    __tablename__ = "student_profiles"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    admission_number: Mapped[str] = mapped_column(String(64), unique=True, index=True, nullable=False)
    date_of_birth: Mapped[date] = mapped_column(Date, nullable=False)
    guardian_name: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    guardian_phone: Mapped[Optional[str]] = mapped_column(String(32), nullable=True)

    enrollments: Mapped[List["Enrollment"]] = relationship(back_populates="student")


class TeacherProfile(Base, TimestampMixin):
    __tablename__ = "teacher_profiles"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    employee_code: Mapped[str] = mapped_column(String(64), unique=True, index=True, nullable=False)
    department_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("departments.id", ondelete="RESTRICT"), nullable=False)

    department: Mapped["Department"] = relationship(back_populates="teachers")
    teaching_assignments: Mapped[List["TeachingAssignment"]] = relationship(back_populates="teacher")


class Enrollment(Base, TimestampMixin):
    __tablename__ = "enrollments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    student_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("student_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False, index=True)
    academic_year_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("academic_years.id", ondelete="RESTRICT"), nullable=False, index=True)
    status: Mapped[EnrollmentStatus] = mapped_column(SQLEnum(EnrollmentStatus), default=EnrollmentStatus.ACTIVE, nullable=False)

    student: Mapped["StudentProfile"] = relationship(back_populates="enrollments")
    section: Mapped["Section"] = relationship(back_populates="enrollments")
    academic_year: Mapped["AcademicYear"] = relationship(back_populates="enrollments")
    subject_enrollments: Mapped[List["StudentSubjectEnrollment"]] = relationship(back_populates="enrollment", cascade="all, delete-orphan")

    __table_args__ = (
        # Section 9.2: Exactly one ACTIVE enrollment per student per academic year
        Index(
            "uq_active_student_academic_year",
            "student_id",
            "academic_year_id",
            unique=True,
            postgresql_where=(status == EnrollmentStatus.ACTIVE)
        ),
    )


class StudentSubjectEnrollment(Base, TimestampMixin):
    __tablename__ = "student_subject_enrollments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    enrollment_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("enrollments.id", ondelete="CASCADE"), nullable=False, index=True)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=False, index=True)

    enrollment: Mapped["Enrollment"] = relationship(back_populates="subject_enrollments")
    subject: Mapped["Subject"] = relationship()

    __table_args__ = (
        UniqueConstraint("enrollment_id", "subject_id", name="uq_enrollment_subject"),
    )


class TeachingAssignment(Base, TimestampMixin):
    __tablename__ = "teaching_assignments"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False, index=True)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False, index=True)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=False, index=True)
    academic_year_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("academic_years.id", ondelete="RESTRICT"), nullable=False)

    teacher: Mapped["TeacherProfile"] = relationship(back_populates="teaching_assignments")
    section: Mapped["Section"] = relationship(back_populates="teaching_assignments")
    subject: Mapped["Subject"] = relationship()

    __table_args__ = (
        UniqueConstraint("section_id", "subject_id", "academic_year_id", name="uq_section_subject_assignment"),
    )


class TeacherSubstitution(Base, TimestampMixin):
    __tablename__ = "teacher_substitutions"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    original_teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False)
    substitute_teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="RESTRICT"), nullable=False)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="RESTRICT"), nullable=False)
    from_date: Mapped[date] = mapped_column(Date, nullable=False)
    to_date: Mapped[date] = mapped_column(Date, nullable=False)


class TimeSlot(Base, TimestampMixin):
    __tablename__ = "time_slots"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    day_of_week: Mapped[int] = mapped_column(Integer, nullable=False)  # 0=Monday, 6=Sunday
    start_time: Mapped[time] = mapped_column(Time, nullable=False)
    end_time: Mapped[time] = mapped_column(Time, nullable=False)
    label: Mapped[Optional[str]] = mapped_column(String(64), nullable=True)


class TimetableEntry(Base, TimestampMixin):
    __tablename__ = "timetable_entries"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    section_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("sections.id", ondelete="CASCADE"), nullable=False, index=True)
    subject_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("subjects.id", ondelete="CASCADE"), nullable=False)
    teacher_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("teacher_profiles.id", ondelete="RESTRICT"), nullable=False)
    time_slot_id: Mapped[uuid.UUID] = mapped_column(GUID(), ForeignKey("time_slots.id", ondelete="RESTRICT"), nullable=False)


class Holiday(Base, TimestampMixin):
    __tablename__ = "holidays"

    id: Mapped[uuid.UUID] = mapped_column(GUID(), primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(128), nullable=False)
    holiday_date: Mapped[date] = mapped_column(Date, unique=True, index=True, nullable=False)
