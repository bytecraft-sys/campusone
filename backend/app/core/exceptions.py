import uuid
from typing import Any, Dict, List, Optional
from fastapi import Request, status
from fastapi.responses import JSONResponse


class CampusOneException(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        status_code: int = status.HTTP_400_BAD_REQUEST,
        details: Optional[List[Any]] = None
    ):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details or []
        super().__init__(self.message)


class NotFoundException(CampusOneException):
    def __init__(self, message: str = "Resource not found", code: str = "not_found"):
        super().__init__(code=code, message=message, status_code=status.HTTP_404_NOT_FOUND)


class ForbiddenException(CampusOneException):
    def __init__(self, message: str = "Not allowed to perform this action", code: str = "forbidden"):
        super().__init__(code=code, message=message, status_code=status.HTTP_403_FORBIDDEN)


class ConflictException(CampusOneException):
    def __init__(self, message: str = "Resource conflict with current state", code: str = "conflict"):
        super().__init__(code=code, message=message, status_code=status.HTTP_409_CONFLICT)


class StaleVersionException(ConflictException):
    def __init__(self, message: str = "Resource has been modified by another process. Please refresh.", code: str = "stale_version"):
        super().__init__(message=message, code=code)


async def campusone_exception_handler(request: Request, exc: CampusOneException) -> JSONResponse:
    request_id = getattr(request.state, "request_id", str(uuid.uuid4()))
    return JSONResponse(
        status_code=exc.status_code,
        headers={"X-Request-Id": request_id},
        content={
            "error": {
                "code": exc.code,
                "message": exc.message,
                "details": exc.details,
                "request_id": request_id
            }
        }
    )
