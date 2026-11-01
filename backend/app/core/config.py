import os
from typing import List, Union
from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    PROJECT_NAME: str = "CampusOne API"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"

    # Database
    DATABASE_URL: str = "sqlite+aiosqlite:///./campusone.db"

    # Cache & Queues
    REDIS_URL: str = "redis://localhost:6379/0"

    # Auth & Tokens
    JWT_SECRET: str = "super-secret-jwt-key-replace-in-production-with-min-64-random-chars"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_MINUTES: int = 15
    REFRESH_TOKEN_DAYS: int = 30
    ACTIVATION_TOKEN_HOURS: int = 72

    # Argon2 Parameters
    ARGON2_TIME_COST: int = 3
    ARGON2_MEMORY_COST: int = 65536
    ARGON2_PARALLELISM: int = 4

    # Object Storage (S3 / MinIO)
    STORAGE_ENDPOINT: str = "http://localhost:9000"
    STORAGE_BUCKET: str = "campusone-uploads"
    STORAGE_KEY: str = "minioadmin"
    STORAGE_SECRET: str = "minioadmin"
    STORAGE_REGION: str = "us-east-1"

    # Antivirus
    CLAMAV_HOST: str = "localhost"
    CLAMAV_PORT: int = 3310

    # CORS
    CORS_ORIGINS: Union[str, List[str]] = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://localhost:8000", "http://10.0.2.2:8000"]

    @field_validator("CORS_ORIGINS", mode="before")
    def assemble_cors_origins(cls, v: Union[str, List[str]]) -> List[str]:
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",") if i.strip()]
        elif isinstance(v, (list, str)):
            return v
        raise ValueError(v)

    # Optional Push and AI
    FCM_CREDENTIALS: str = ""
    AI_API_KEY: str = ""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore"
    )


settings = Settings()
