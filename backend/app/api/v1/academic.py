import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, File, UploadFile, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user, require_admin
from app.db.session import get_db
from app.models.identity import User
from app.schemas.academic import (
    AcademicYearCreate,
    AcademicYearResponse,
    AdminUserCreate,
    AdminUserCreateResponse,
    CourseCreate,
    CourseResponse,
    CourseSubjectCreate,
    CourseSubjectResponse,
    CSVImportDryRunReport,
    DepartmentCreate,
    DepartmentResponse,
    EnrollmentCreate,
    EnrollmentResponse,
    SectionCreate,
    SectionResponse,
    SubjectCreate,
    SubjectResponse,
    TeachingAssignmentCreate,
    TeachingAssignmentResponse,
    TimeSlotCreate,
    TimeSlotResponse,
    TimetableEntryCreate,
    TimetableEntryResponse,
    TodayScheduleItem,
)
from app.services.academic_service import AcademicService

router = APIRouter(tags=["Academic Structure & Administration"])


# --- Academic Years ---
@router.post("/admin/academic-years", response_model=AcademicYearResponse, status_code=status.HTTP_201_CREATED)
async def create_academic_year(
    payload: AcademicYearCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_academic_year(payload)


@router.get("/admin/academic-years", response_model=List[AcademicYearResponse])
async def list_academic_years(
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_academic_years()


# --- Departments ---
@router.post("/admin/departments", response_model=DepartmentResponse, status_code=status.HTTP_201_CREATED)
async def create_department(
    payload: DepartmentCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_department(payload)


@router.get("/admin/departments", response_model=List[DepartmentResponse])
async def list_departments(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_departments()


# --- Courses ---
@router.post("/admin/courses", response_model=CourseResponse, status_code=status.HTTP_201_CREATED)
async def create_course(
    payload: CourseCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_course(payload)


@router.get("/admin/courses", response_model=List[CourseResponse])
async def list_courses(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_courses()


# --- Subjects ---
@router.post("/admin/subjects", response_model=SubjectResponse, status_code=status.HTTP_201_CREATED)
async def create_subject(
    payload: SubjectCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_subject(payload)


@router.get("/admin/subjects", response_model=List[SubjectResponse])
async def list_subjects(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_subjects()


# --- Course Subject Mapping ---
@router.post("/admin/course-subjects", response_model=CourseSubjectResponse, status_code=status.HTTP_201_CREATED)
async def map_course_subject(
    payload: CourseSubjectCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.map_course_subject(payload)


@router.get("/admin/course-subjects", response_model=List[CourseSubjectResponse])
async def list_course_subjects(
    course_id: Optional[uuid.UUID] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_course_subjects(course_id)


# --- Sections ---
@router.post("/admin/sections", response_model=SectionResponse, status_code=status.HTTP_201_CREATED)
async def create_section(
    payload: SectionCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_section(payload)


@router.get("/admin/sections", response_model=List[SectionResponse])
async def list_sections(
    course_id: Optional[uuid.UUID] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_sections(course_id)


# --- Allocations & Timetable ---
@router.post("/admin/teaching-assignments", response_model=TeachingAssignmentResponse, status_code=status.HTTP_201_CREATED)
async def create_teaching_assignment(
    payload: TeachingAssignmentCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_teaching_assignment(payload)


@router.get("/admin/teaching-assignments", response_model=List[TeachingAssignmentResponse])
async def list_teaching_assignments(
    section_id: Optional[uuid.UUID] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.list_teaching_assignments(section_id)


@router.post("/admin/time-slots", response_model=TimeSlotResponse, status_code=status.HTTP_201_CREATED)
async def create_time_slot(
    payload: TimeSlotCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_time_slot(payload)


@router.post("/admin/timetable", response_model=TimetableEntryResponse, status_code=status.HTTP_201_CREATED)
async def create_timetable_entry(
    payload: TimetableEntryCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.create_timetable_entry(payload)


@router.get("/timetable/today", response_model=List[TodayScheduleItem])
async def get_today_timetable(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.get_today_schedule(current_user)


# --- Enrollments ---
@router.post("/admin/enrollments", response_model=EnrollmentResponse, status_code=status.HTTP_201_CREATED)
async def enroll_student(
    payload: EnrollmentCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    service = AcademicService(db)
    return await service.enroll_student(payload)


# --- User Provisioning & CSV Import ---
@router.post("/admin/users", response_model=AdminUserCreateResponse, status_code=status.HTTP_201_CREATED)
async def create_user_by_admin(
    payload: AdminUserCreate,
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Admin creates account. Returns 72-hour single-use activation link."""
    service = AcademicService(db)
    return await service.create_user_by_admin(current_user, payload)


@router.get("/admin/users")
async def list_users(
    db: AsyncSession = Depends(get_db),
):
    """List all accounts from database."""
    service = AcademicService(db)
    users = await service.list_users()
    return [
        {
            "id": str(u.id),
            "loginId": u.login_id,
            "name": u.full_name,
            "role": u.role.value.capitalize(),
            "dept": "Section A" if u.role.value == "STUDENT" else "CSE",
            "status": {"type": "good" if u.is_active else "info", "label": "Active" if u.is_active else "Pending activation"},
            "lastLogin": "Today" if u.is_active else "-"
        }
        for u in users
    ]


@router.post("/admin/users/csv-dry-run", response_model=CSVImportDryRunReport)
async def dry_run_csv_import(
    file: UploadFile = File(...),
    current_user: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Section 3.3: dry-run error report for CSV bulk user imports before confirming."""
    content = (await file.read()).decode("utf-8")
    service = AcademicService(db)
    return await service.dry_run_csv_import(content)

