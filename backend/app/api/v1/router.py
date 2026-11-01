from fastapi import APIRouter
from app.api.v1.auth import router as auth_router
from app.api.v1.attendance import router as attendance_router
from app.api.v1.leave import router as leave_router
from app.api.v1.assignments import router as assignments_router
from app.api.v1.academic import router as academic_router
from app.api.v1.platform import router as platform_router

api_v1_router = APIRouter()
api_v1_router.include_router(auth_router)
api_v1_router.include_router(attendance_router)
api_v1_router.include_router(leave_router)
api_v1_router.include_router(assignments_router)
api_v1_router.include_router(academic_router)
api_v1_router.include_router(platform_router)





