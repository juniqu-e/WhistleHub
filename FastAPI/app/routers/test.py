"""
test용 router
"""
from fastapi import APIRouter
import config
import utils
from common import ApiResponse, ResponseType
from common.exceptions import CustomException

import app.models.request as req
import app.models.response as res

import app.services.test as test_service

router = APIRouter(prefix=f"{config.API_BASE_URL}/test", tags=["test"])


@router.get(
    "/",
    summary="테스트 Get API",
    description="hello world를 출력",
    response_model=ApiResponse[str]
)
@utils.logger()
async def success_endpoint():
    return ApiResponse(payload="hello world")


@router.get(
    "/error",
    summary="테스트 Error Get API",
    description="Not Found Page Error를 발생시킴",
)
@utils.logger()
async def error_endpoint():
    raise CustomException(ResponseType.NOT_FOUND_PAGE)


@router.get(
    "/item",
    summary="테스트용 item API",
    description="이름과 나이를 입력하면 이름, 나이, 메시지를 반환",
    response_model=ApiResponse[res.TestResponseDto]
)
def item(testRequestDto: req.TestRequestDto) -> int:
    return ApiResponse(payload=test_service.get_item(testRequestDto))
