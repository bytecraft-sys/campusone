import uuid
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse


class RequestIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # Extract existing or generate new X-Request-Id
        request_id = request.headers.get("X-Request-Id", str(uuid.uuid4()))
        request.state.request_id = request_id

        try:
            response = await call_next(request)
            response.headers["X-Request-Id"] = request_id
            return response
        except Exception as exc:
            # If an unhandled exception bubbles up, return formatted JSON response
            return JSONResponse(
                status_code=500,
                headers={"X-Request-Id": request_id},
                content={
                    "error": {
                        "code": "internal_server_error",
                        "message": "An unexpected server error occurred.",
                        "details": [str(exc)],
                        "request_id": request_id
                    }
                }
            )
