from pydantic import BaseModel, Field


class TestResponseDto(BaseModel):
    """
    TestResponseDto
    테스트용 response Dto

    Attributes:
    ---
    name: str
        이름
    age: int
        나이
    msg: str
        메시지
    """
    name:str = Field(..., title="이름", description="이름을 입력해주세요.")
    age:str = Field(..., title="나이", description="나이를 입력해주세요.")
    msg:str = Field(..., title="메시지", description="메시지를 입력해주세요.")
