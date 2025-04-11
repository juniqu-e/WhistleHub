from enum import Enum
from fastapi import status


class ResponseType(Enum):
    """
    응답의 타입을 정의한 Enum 클래스

    Attributes
    ---
    code: str
        응답 코드
    status_code: int
        HTTP 상태 코드
    message: str
        응답 메시지
    """

    # HTTP Status 200
    SUCCESS = ("SU",
               status.HTTP_200_OK,
               "요청이 성공적으로 처리되었습니다."
               )

    # HTTP Status 400
    BAD_REQUEST = ("BR",
                   status.HTTP_400_BAD_REQUEST,
                   "잘못된 요청입니다."
                   )

    # HTTP Status 401
    UNAUTHORIZED = ("UNA",
                    status.HTTP_401_UNAUTHORIZED,
                    "인증되지 않은 사용자입니다."
                    )

    # HTTP Status 404
    NOT_FOUND_PAGE = ("NFP",
                      status.HTTP_404_NOT_FOUND,
                      "페이지를 찾을 수 없습니다."
                      )

    # HTTP Status 500
    SERVER_ERROR = ("SER",
                    status.HTTP_500_INTERNAL_SERVER_ERROR,
                    "서버 내부 오류가 발생했습니다."
                    )

    # 추가적인 exception을 정의

    def __init__(self, code: str, status_code: int, message: str):
        self.code = code
        self.status_code = status_code
        self.message = message
