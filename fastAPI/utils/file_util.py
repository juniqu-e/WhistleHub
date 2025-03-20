"""
파일 관련 유틸리티 함수를 모아놓은 파일
"""
import os
import time
from typing import Union


def get_new_file_path(file_name: str):
    """
    새 파일의 경로를 반환하는 함수

    Params
    ---
    file_name: str
        파일 이름
    """
    return f"./app/files/{time.time_ns()}_{file_name}"


def get_file_size(file_path: str):
    """
    파일의 크기를 반환하는 함수

    Params
    ---
    file_path: str
        파일 경로
    """
    return os.path.getsize(file_path)


def save_file(file: Union[str, bytes], save_mode: str = None, file_name: str = None):
    """
    파일을 저장한 후 저장된 파일의 경로를 반환하는 함수

    Params
    ---
    file: str, bytes
        저장할 파일
    save_mode: str
        저장 모드
    file_name: str
        저장할 파일 이름
    """
    if file_name is None:  # 파일 이름이 없는 경우
        raise ValueError("file_name is required")
    if "." not in file_name:  # 파일 이름에 확장자가 없는 경우
        raise ValueError("file_name must have extension")

    if isinstance(file, bytes):
        save_mode = "wb" if save_mode is None else save_mode
    else:
        save_mode = "w" if save_mode is None else save_mode

    file_path = get_new_file_path(file_name)
    with open(file_path, save_mode) as f:
        f.write(file)

    return file_path


def delete_file(file_path: str):
    """
    파일을 삭제하는 함수

    Params
    ---
    file_path: str
        삭제할 파일 경로
    """
    if os.path.exists(file_path):
        os.remove(file_path)
        return True
    else:
        return False


def delete_files(file_path: list[str]):
    """
    파일을 삭제하는 함수

    Params
    ---
    file_path: str
        삭제할 파일 경로
    """
    for path in file_path:
        if delete_file(path) == False:
            return False
    return True
