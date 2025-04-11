"""
에러 핸들러 추가 모듈
"""
from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import HTTPException
from starlette.exceptions import HTTPException as StarletteHTTPException

from .CustomException import CustomException
from common.ResponseType import ResponseType

from utils.logger import log
import logging


def add_exception_handler(app: FastAPI):
    """
    에러 핸들러 추가함수

    Params
    ---
    app: FastAPI
        에러 핸들러를 추가할 FastAPI 객체
    """
    @app.exception_handler(CustomException)
    async def custom_exception_handler(request: Request, exc: CustomException):
        error_status = exc.response_type
        error_status.message = exc.message

        log(f"CustomException: {error_status.code} - {error_status.message}",
            level=logging.ERROR)

        return make_json_error_response(error_status)

    @app.exception_handler(StarletteHTTPException)
    async def general_http_exception_handler(request: Request, exc: StarletteHTTPException):
        if exc.status_code == status.HTTP_404_NOT_FOUND:
            error_status = ResponseType.NOT_FOUND_PAGE
        elif exc.status_code == status.HTTP_401_UNAUTHORIZED:
            error_status = ResponseType.UNAUTHORIZED
        else:
            error_status = ResponseType.SERVER_ERROR  # 미리 정의해둔 GENERAL_ERROR 사용

        log(f"StarletteHTTPException: {error_status.code} - {error_status.message}",
            level=logging.ERROR)

        return make_json_error_response(error_status)

    @app.exception_handler(HTTPException)
    async def validation_exception_handler(request: Request, exc: HTTPException):
        if exc.status_code == status.HTTP_400_BAD_REQUEST:
            error_status = ResponseType.BAD_REQUEST
        elif exc.status_code == status.HTTP_404_NOT_FOUND:
            error_status = ResponseType.NOT_FOUND_PAGE
        elif exc.status_code == status.HTTP_401_UNAUTHORIZED:
            error_status = ResponseType.UNAUTHORIZED
        else:
            error_status = ResponseType.SERVER_ERROR

        log(f"HTTPException: {error_status.code} - {error_status.message}",
            level=logging.ERROR)

        return make_json_error_response(error_status)


def make_json_error_response(error_status: ResponseType) -> JSONResponse:
    """
    JSONResponse 객체를 생성하는 함수

    Params
    ---
    error_status: ResponseType
        응답 코드

    Returns
    ---
    JSONResponse
        생성된 JSONResponse 객체
    """
    return JSONResponse(
        status_code=error_status.status_code,
        content={
            "code": error_status.code,
            "message": error_status.message,
            "object": None
        }
    )
