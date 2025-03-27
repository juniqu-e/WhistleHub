"""
test용 service
"""
import app.models.request as req
import app.models.response as res


def get_hello_world() -> str:
    return "hello world"


def sum(a: int, b: int) -> int:
    return a + b


def get_item(testRequestDto: req.TestRequestDto) -> res.TestResponseDto:
    return res.TestResponseDto(name=testRequestDto.name, age=testRequestDto.age, msg=get_hello_world())
