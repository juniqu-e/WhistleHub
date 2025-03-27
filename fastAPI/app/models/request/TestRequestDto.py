from pydantic import BaseModel, Field


class TestRequestDto(BaseModel):
    """
    TestRequestDto
    테스트용 request Dto

    Attributes:
    ---
    name: str
        이름

    age: int
        나이
    """
    name: str = Field(..., title="이름", description="이름을 입력해주세요.")
    age: int = Field(..., title="나이", description="나이를 입력해주세요.")
