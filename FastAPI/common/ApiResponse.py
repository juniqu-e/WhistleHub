from typing import Generic, TypeVar, Optional
from pydantic import BaseModel, Field
from .ResponseType import ResponseType

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    """
    API 공통 응답 클래스

    Attributes
    ---
    code: str
        응답 코드 (2~4글자)
    message: str
        응답 메시지
    object: Optional[T]
        응답 객체
    """
    code: str = Field(ResponseType.SUCCESS.code, description="응답 코드")
    message: str = Field(ResponseType.SUCCESS.message, description="응답 메시지")
    payload: Optional[T] = Field(None, description="응답 객체")
