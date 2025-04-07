import config
import logging
import utils
import os
import uuid

from fastapi import APIRouter, UploadFile, File, Form, Query, Path, HTTPException, status, BackgroundTasks
from celery import chain
from typing import List, Optional
from pydantic import BaseModel

from common import ApiResponse, ResponseType
from common.exceptions import CustomException

from utils.file_util import *

from app.services.UseOpenl3 import *
from app.services.openl3Tasks import *
from app.models.request.RecommendTrackRequestDto import *


router = APIRouter(prefix=f"{config.API_BASE_URL}/track", tags=["audio"])

# OpenL3Service 인스턴스 생성
openl3_service = OpenL3Service()

# 공유 오디오 디렉토리 정의 및 생성
SHARED_AUDIO_DIR = config.SHARED_AUDIO_DIR
os.makedirs(SHARED_AUDIO_DIR, exist_ok=True)

@router.post(
    "/upload/async",
    summary="비동기 오디오 파일 처리 API (Celery)",
    description="오디오 파일을 Celery를 통해 비동기로 처리하고 결과를 Callback URL로 전송합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def async_process_audio_celery(

    audio: UploadFile = File(..., description="처리할 오디오 파일"),
    trackId: Optional[int] = Form(None, description="RDB 트랙 ID"),
    instrumentTypes: Optional[List[int]] = Form(None, description="악기 종류"),
    limit: int = Form(5, description="반환할 결과 수", gt=0, le=100),
    callbackUrl: str = Form(..., description="결과를 받을 Callback URL")
):
    supported_formats = ['.wav', '.mp3', '.flac', '.ogg', '.m4a', '.aac']
    file_ext = os.path.splitext(audio.filename.lower())[1]
    if not file_ext or file_ext not in supported_formats:
        raise CustomException(
            ResponseType.BAD_REQUEST,
            f"지원하지 않는 파일 형식입니다. 지원 형식: {', '.join(supported_formats)}"
        )

    file_path = None
    try:
        # 파일 경로 생성 로직 변경

        new_file_name = get_new_file_name(audio.filename)

        # 파일 저장 경로 설정정
        file_path = os.path.join(SHARED_AUDIO_DIR, new_file_name)

        # 파일 컨텐츠 읽기
        file_content = await audio.read()

        # 파일 저장
        save_file_with_path(file_path, file_content, save_mode="wb")
        
        # 파일 크기 확인
        file_size = get_file_size(file_path)

        # 파일 크기가 0인 경우 예외 처리
        if file_size == 0:
            if file_path and os.path.exists(file_path):
                os.remove(file_path)
            raise CustomException(ResponseType.BAD_REQUEST, "빈 파일이 업로드되었습니다.")

        utils.log(f"Celery 처리를 위해 파일 저장: {file_path}. 크기: {file_size} bytes", level=logging.DEBUG)

        # 순차 실행을 위한 Celery 체인 생성
        # .s() 시그니처 메서드를 사용하여 필요한 인자 전달
        # 주의: 한 작업의 결과는 다음 작업의 *첫 번째* 인자로 전달됩니다.
        # task_send_callback에는 trackId를 명시적으로 전달해야 합니다.
        processing_chain = chain(
            # temp_file_path와 track_id 전달
            task_process_audio.s(temp_file_path=file_path, track_id=trackId, instrumentTypes=instrumentTypes) |
            # limit 전달 (이전 결과인 track_id가 첫 인자로 자동 전달됨)
            task_find_similar.s(limit=limit) |
            # callback_url과 trackId 명시적 전달
            # find_similar의 결과(similar_tracks)가 첫 인자로 자동 전달됨
            task_send_callback.s(callback_url=callbackUrl, track_id=trackId)
        )

        # 메인 체인 *뒤에* 실행될 정리 작업 정의
        # link를 사용하면 체인이 성공한 경우에만 실행됩니다.
        # 오류 발생 시에도 보장된 정리(예: 파일 삭제)를 위해서는 Celery의 시그널이나 오류 연결(link_error) 고려 필요.
        # 여기서는 단순화를 위해 체인으로 연결합니다. send_callback이 실패하면 cleanup이 실행되지 않을 수 있습니다.
        full_chain = chain(processing_chain | task_cleanup_file.si(temp_file_path=file_path))

        # 체인을 비동기적으로 실행
        task_result = full_chain.apply_async()

        utils.log(f"Celery 작업 체인 시작됨. ID: {task_result.id}", level=logging.INFO)

        # 즉시 응답 반환
        return ApiResponse(payload={
            "message": "오디오 파일 처리가 Celery 백그라운드에서 시작되었습니다.",
            "taskId": task_result.id, # Celery 작업 ID 반환
            "file": audio.filename,
            "trackId": trackId,
            "callbackUrl": callbackUrl
        })

    except Exception as e:
        # 제출 중 오류 발생 시 임시 파일 정리
        if file_path and os.path.exists(file_path):
            try:
                delete_file(file_path)
            except OSError:
                utils.log(f"제출 오류 후 임시 파일({file_path}) 정리 실패.", level=logging.ERROR)
        utils.log(f"Celery 작업 제출 중 오류 발생: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"Celery 작업 제출 중 오류 발생: {str(e)}")
    

@router.post(
    "/ai/recommend",
    summary="추천 트랙 API",
    description="악기 종류에 따라 추천 트랙을 반환합니다.",
    response_model=int | None,
)
@utils.logger()
async def recommend_track(request: RecommendTrackRequest):
    """
    추천 트랙을 반환하는 API입니다.
    필요 악기 종류와 기존 트랙 ID를 기반으로 추천 트랙을 찾습니다.
    """
    try:
        utils.log(f"추천 트랙 요청: {request}", level=logging.DEBUG)

        # OpenL3Service의 find_similar_by_track_id 메서드를 사용하여 추천 트랙 검색
        recommended_track_id = openl3_service.find_similar_by_instrument_type(
            needInstrumentTypes=request.needInstrumentTypes,
            trackIds=request.trackIds
        )
        return recommended_track_id

    except Exception as e:
        utils.log(f"추천 트랙 검색 중 오류 발생: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"추천 트랙 검색 중 오류 발생: {str(e)}")