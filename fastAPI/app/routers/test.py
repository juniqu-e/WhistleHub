"""
test용 router
"""
from fastapi import APIRouter
import config
import utils
from common import ApiResponse, ResponseType
from common.exceptions import CustomException

router = APIRouter(prefix=f"{config.API_BASE_URL}/test", tags=["test"])


@router.get(
    "/",
    summary="테스트 Get API",
    description="hello world를 출력",
    response_model=ApiResponse[str]
)
@utils.logger()
async def success_endpoint():
    return ApiResponse(object="hello world")


@router.get(
    "/error",
    summary="테스트 Error Get API",
    description="Not Found Page Error를 발생시킴",
)
@utils.logger()
async def error_endpoint():
    raise CustomException(ResponseType.SERVER_ERROR)
