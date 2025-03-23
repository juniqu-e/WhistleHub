"""
logger 관련 유틸리티를 모아놓은 파일
"""
import logging
import time
import functools
import inspect
import json
import re
import datetime
from fastapi import Request
from pydantic import BaseModel

uvcron_logger = logging.getLogger("uvcorn.info")


def parse_request(request):
    """
    Request를 파싱하는 함수

    Params
    ---
    request: Request
        FastAPI의 Request 객체
    """
    body_str = request.decode("utf-8")  # 문자열 변환
    try:
        parsed_json = json.loads(body_str)  # JSON 변환
        return parsed_json
    except json.JSONDecodeError:
        print("Received Non-JSON Body:", body_str)  # JSON이 아니면 그냥 출력
        return body_str


CUT_MODE = True


def cut_string(string: str, length: int = 20):
    """
    문자열을 자르는 함수

    Params
    ---
    string: str
        자를 문자열
    length: int
        자를 길이
    """

    if len(string) > length and CUT_MODE:
        string = string[:length] + "..."

    return string


PRETTY_PRINT_DICT = True


def pretty_print_dict(d, indent=0, base_space_cnt=2):
    """
    딕셔너리를 보기 좋게 출력하는 헬퍼 함수.

    Params
    ---
    d: dict
        출력할 딕셔너리
    indent: int
        들여쓰기 칸 수
    base_space_cnt: int
        기본 들여쓰기 칸 수
    """
    if not PRETTY_PRINT_DICT:
        return str(d)

    # 들여쓰기 공백 생성
    base_space = " " * base_space_cnt
    spaces = base_space * indent

    # 딕셔너리 출력
    lines = []
    lines.append(spaces + "{")
    for key, value in d.items():  # 딕셔너리의 키-값 쌍을 출력
        if isinstance(value, dict):  # 값이 딕셔너리인 경우
            lines.append(spaces + f"{base_space}{key}:")
            lines.append(pretty_print_dict(value, indent + 1))
        elif isinstance(value, list):  # 값이 리스트인 경우
            lines.append(spaces + f"{base_space}{key}: [")
            for item in value:
                if isinstance(item, dict):  # 리스트의 원소가 딕셔너리인 경우
                    lines.append(pretty_print_dict(item, indent + 2))
                else:  # 리스트의 원소가 딕셔너리가 아닌 경우
                    lines.append(spaces + f"{base_space}{item},")
            lines.append(spaces + f"{base_space}],")  # 리스트 닫기
        else:  # 그 외의 경우
            print_value = cut_string(str(value))
            lines.append(spaces + f"{base_space}{key}: {print_value},")
    lines.append(spaces + "}")
    return "\n".join(lines)


def print_value(value):
    """
    함수의 파라미터 이름과 인자값을 출력하는 함수

    Params
    ---
    value: any
        출력할 값
    """
    # 매핑된 인자들을 함수의 파라미터 이름에 맞춰 출력
    if value is None:
        print("None")
        return

    if isinstance(value, BaseModel):  # Pydantic 모델 객체
        print(pretty_print_dict(value.model_dump()))
    elif isinstance(value, dict):  # 딕셔너리
        print(pretty_print_dict(value))
    elif hasattr(value, "filename") and hasattr(value, "content_type"):  # UploadFile 또는 이와 유사한 객체로 간주
        # UploadFile 또는 이와 유사한 객체로 간주
        file_info = {
            "filename": getattr(value, "filename", None),
            "content_type": getattr(value, "content_type", None)
        }
        print(pretty_print_dict(file_info))
    else:  # 그 외의 경우
        print(cut_string(str(value)))


def logger(_func=None, *, print_input=False, print_output=False):
    """
    함수의 입력과 출력을 로그로 남기는 데코레이터

    Params
    ---
    func: Callable
        데코
    """
    def decorator_logger(func):
        # 함수가 코루틴(비동기 함수)인지 확인
        if inspect.iscoroutinefunction(func):
            # 비동기 함수 래핑
            @functools.wraps(func)
            async def async_wrapper(*args, **kwargs):
                # 함수의 인자를 바인딩
                sig = inspect.signature(func)
                bound = sig.bind(*args, **kwargs)
                bound.apply_defaults()

                if print_input:
                    # 함수의 입력 출력
                    print("input")
                    if not bound.arguments:
                        print("No input")
                    for param_name, value in bound.arguments.items():
                        print(f"{param_name}:")
                        if isinstance(value, Request):  # Request 객체인 경우
                            body_byte = await value.body()
                            body = json.loads(body_byte.decode("utf-8"))

                            print(pretty_print_dict(body))
                        else:  # Request 객체가 아닌 경우
                            print_value(value)
                log(f"{func.__name__}() start")
                start_time = time.time_ns()
                # 비동기 함수 호출
                result = await func(*args, **kwargs)
                end_time = time.time_ns()
                elapsed_time_seconds = (end_time - start_time) / 1_000_000_000
                log(f"{func.__name__}() end  | {elapsed_time_seconds:.2f} seconds")

                if print_output:
                    # 함수의 출력
                    print("output")
                    print_value(result)
                return result
            return async_wrapper
        else:
            # 동기 함수 래핑
            @functools.wraps(func)
            def sync_wrapper(*args, **kwargs):
                # 함수의 인자를 바인딩
                sig = inspect.signature(func)
                bound = sig.bind(*args, **kwargs)
                bound.apply_defaults()

                if print_input:
                    # 함수의 입력 출력
                    print("input")
                    if not bound.arguments:  # 입력이 없는 경우
                        print("No input")
                    for param_name, value in bound.arguments.items():  # 입력이 있는 경우
                        print(f"{param_name}:")
                        print_value(value)

                log(f"{func.__name__}() start")
                start_time = time.time_ns()
                # 동기 함수 호출
                result = func(*args, **kwargs)
                end_time = time.time_ns()
                elapsed_time_seconds = (end_time - start_time) / 1_000_000_000
                log(f"{func.__name__}() end   | {elapsed_time_seconds:.2f} seconds")
                if print_output:
                    # 함수의 출력
                    print("output")
                    print_value(result)
                return result
            return sync_wrapper

    # 데코레이터가 파라미터 없이 바로 적용된 경우 (_func가 함수일 경우)
    if _func is None:
        return decorator_logger
    else:
        return decorator_logger(_func)


def log(msg: str, level: int = logging.INFO):
    """
    로그를 남기는 함수

    Params
    ---
    message: str
        로그 메시지
    level: int
        로그 레벨
    """
    uvcron_logger.log(msg=msg, level=level)


class LogConfig:
    """
    logger 데코레이터의 설정을 변경하는 클래스

    Attributes
    ---
    CUT_MODE: bool
        문자열 자르기 설정
    PRETTY_PRINT_DICT: bool
        딕셔너리 출력 설정

    Params
    ---
    active_log_file: bool
        로그 파일 활성화 여부
    file_name: str
        로그 파일 이름
    mode: str
        파일 열기 모드
    string_cut_mode: bool
        문자열 자르기 설정
    pretty_print_dict_mode: bool
        딕셔너리 출력 설정
    """

    def __init__(self, string_cut_mode: bool = True, pretty_print_dict_mode: bool = True):
        global CUT_MODE, PRETTY_PRINT_DICT
        CUT_MODE = string_cut_mode
        PRETTY_PRINT_DICT = pretty_print_dict_mode
