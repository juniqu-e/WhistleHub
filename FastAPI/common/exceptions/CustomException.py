from typing import Optional
from common.ResponseType import ResponseType


class CustomException(Exception):
    """
    사용자 정의 예외 클래스

    Attributes
    ---
    response_type: ResponseType
        응답 타입
    message: str
        예외 메세지 (기본값은 ResponseType의 message)
    """

    def __init__(self, response_type: ResponseType, message: Optional[str] = None):
        self.response_type = response_type
        # message가 제공되지 않으면 ResponseType의 기본 메시지를 사용합니다.
        self.message = message or response_type.message
