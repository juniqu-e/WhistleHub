from fastapi import APIRouter, UploadFile, File, Form, Query, Path, HTTPException, status, BackgroundTasks
import config
import logging
import utils
from common import ApiResponse, ResponseType
from common.exceptions import CustomException
import os
import tempfile
from typing import List, Optional
import json

from app.services.UseOpenl3 import OpenL3Service
from app.services.callback import process_audio_in_background

router = APIRouter(prefix=f"{config.API_BASE_URL}/track", tags=["audio"])

# OpenL3Service 인스턴스 생성
openl3_service = OpenL3Service()

# 오디오 파일 처리 API
@router.post(
    "/upload",
    summary="오디오 파일 처리 API",
    description="오디오 파일을 업로드하고 임베딩을 추출하여 Milvus에 저장합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def process_audio(
    audio: UploadFile = File(..., description="처리할 오디오 파일"),
    trackId: Optional[int] = Form(None, description="RDB 트랙 ID")
):
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출 및 milvus에에 저장 (trackId 전달)
            milvusId = openl3_service.process_audio_file(temp_file.name, trackId)
            
            if not milvusId:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")
            
            return ApiResponse(payload={
                "id": milvusId,  # Milvus 내부 ID
                "trackId": trackId,  # 외부 트랙 ID 
                "filename": audio.filename
            })
        
        except Exception as e:
            utils.log(f"Error processing audio file: {str(e)}", level=logging.ERROR)
            raise CustomException(ResponseType.SERVER_ERROR, f"오디오 처리 중 오류 발생: {str(e)}")
        
        finally:
            # 임시 파일 삭제
            if os.path.exists(temp_file.name):
                os.remove(temp_file.name)


# 트랙 ID로 유사도 검색 API
@router.get(
    "/similar/{trackId}",
    summary="RDB 트랙 ID로 유사한 트랙 검색 API",
    description="RDB 트랙 ID를 기준으로 유사한 오디오 트랙을 검색합니다",
    response_model=ApiResponse[List[dict]]
)
@utils.logger()
async def find_similar_by_track_id(
    trackId: int = Path(..., description="검색 기준 RDB 트랙 ID"),
    limit: int = Query(5, description="반환할 결과 수", gt=0, le=100)
):
    try:
        similarTracks = openl3_service.find_similar_by_track_id(trackId, limit)
        return ApiResponse(payload=similarTracks)
    
    except Exception as e:
        utils.log(f"Error finding similar tracks by ID: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"유사 트랙 검색 중 오류 발생: {str(e)}")


# 오디오 업로드 및 유사 트랙 검색 API
@router.post(
    "/similar",
    summary="오디오 업로드 및 유사 트랙 검색 API",
    description="오디오 파일을 업로드하고 유사한 트랙을 바로 검색합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def upload_and_find_similar(
    audio: UploadFile = File(..., description="검색 기준 오디오 파일"),
    trackId: Optional[int] = Form(None, description="RDB 트랙 ID"),
    limit: int = Form(5, description="반환할 결과 수", gt=0, le=100)
):
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출 및 milvus에에 저장 (trackId 전달)
            milvusId = openl3_service.process_audio_file(temp_file.name, trackId)
            
            if not milvusId:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")

            
            # 트랙 ID로 유사도 검색
            similarTracks = openl3_service.find_similar_by_track_id(trackId, limit)
            
            return ApiResponse(payload={
                "processedFile": {
                    "milvusId": milvusId,
                    "trackId": trackId,
                    "filename": audio.filename
                },
                "similarTracks": similarTracks
            })
        
        except Exception as e:
            utils.log(f"Error processing audio and finding similar tracks: {str(e)}", level=logging.ERROR)
            raise CustomException(ResponseType.SERVER_ERROR, f"오디오 처리 중 오류 발생: {str(e)}")
        
        finally:
            # 임시 파일 삭제
            if os.path.exists(temp_file.name):
                os.remove(temp_file.name)


@router.post(
    "/upload/batch",
    summary="다중 오디오 파일 처리 API",
    description="여러 오디오 파일을 업로드하고 임베딩을 추출하여 Milvus에 저장합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def batch_process_audio_files(
    files: List[UploadFile] = File(..., description="처리할 오디오 파일들"),
    metadata: str = Form(..., description="JSON 형식의 파일 메타데이터 {파일명: trackId, 파일명: trackId, ...}")
):
    # 1. JSON 파싱, 파일명과 메타데이터 검증
    try:
        fileMetadata = json.loads(metadata)
    except json.JSONDecodeError as e:
        utils.log(f"Invalid JSON metadata: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.BAD_REQUEST, f"올바르지 않은 JSON 형식: {str(e)}")

    uploaded_filenames = {file.filename for file in files}
    metadata_filenames = set(fileMetadata.keys())
    
    missing_metadata = uploaded_filenames - metadata_filenames
    if missing_metadata:
        utils.log(f"Missing metadata for files: {missing_metadata}", level=logging.WARNING)
        raise CustomException(
            ResponseType.BAD_REQUEST, 
            f"다음 파일에 대한 메타데이터가 없습니다: {', '.join(missing_metadata)}"
        )
    
    # 2. 파일 처리 로직
    try:
        results = {}
        for file in files:
            # 임시 파일로 저장
            with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(file.filename)[1]) as temp_file:
                try:
                    # 파일 저장
                    contents = await file.read()
                    temp_file.write(contents)
                    temp_file.flush()
                    
                    # 메타데이터에서 trackId 찾기
                    trackId = fileMetadata.get(file.filename)
                    if trackId is None:
                        utils.log(f"No trackId found for file: {file.filename}", level=logging.WARNING)
                        results[file.filename] = {
                            "milvusId": None,
                            "trackId": None,
                            "error": "메타데이터에 trackId가 없습니다"
                        }
                        continue
                    
                    # 임베딩 추출 및 저장
                    milvusId = openl3_service.process_audio_file(temp_file.name, trackId)
                    results[file.filename] = {
                        "milvusId": milvusId,
                        "trackId": trackId,
                        "status": "success" if milvusId else "failed"
                    }
                    
                except Exception as file_error:
                    utils.log(f"Error processing file {file.filename}: {str(file_error)}", level=logging.ERROR)
                    results[file.filename] = {
                        "milvusId": None,
                        "trackId": fileMetadata.get(file.filename),
                        "error": str(file_error)
                    }
                
                finally:
                    # 임시 파일 삭제
                    if os.path.exists(temp_file.name):
                        os.remove(temp_file.name)
        
        # 결과 통계 계산
        failedCount = sum(1 for item in results.values() if item.get("milvusId") is None)
        successCount = len(results) - failedCount
        
        return ApiResponse(payload={
            "message": f"{len(results)}개 파일 중 {successCount}개 처리 성공, {failedCount}개 실패",
            "results": results
        })
    
    except Exception as e:
        utils.log(f"Error batch processing audio files: {str(e)}", level=logging.ERROR)
        import traceback
        traceback.print_exc()
        raise CustomException(ResponseType.SERVER_ERROR, f"다중 오디오 파일 처리 중 오류 발생: {str(e)}")


@router.post(
    "/upload/async",
    summary="비동기 오디오 파일 처리 API",
    description="오디오 파일을 비동기로 임베딩을 추출하여 Milvus에 저장하고 결과를 Callback URL로 전송합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def async_process_audio(
    background_tasks: BackgroundTasks,
    audio: UploadFile = File(..., description="처리할 오디오 파일"),
    trackId: Optional[int] = Form(None, description="RDB 트랙 ID"),
    limit: int = Form(5, description="반환할 결과 수", gt=0, le=100),
    callbackUrl: str = Form(..., description="결과를 받을 Callback URL")
):
    # 지원되는 파일 형식 확인
    supported_formats = ['.wav', '.mp3', '.flac', '.ogg', '.m4a', '.aac']
    file_ext = os.path.splitext(audio.filename.lower())[1]
    
    if not file_ext or file_ext not in supported_formats:
        raise CustomException(
            ResponseType.BAD_REQUEST, 
            f"지원하지 않는 파일 형식입니다. 지원 형식: {', '.join(supported_formats)}"
        )

    # 임시 파일로 저장 (처리 후 삭제하지 않음 - 백그라운드에서 사용)
    temp_file_path = None
    try:
        # 임시 파일 생성
        suffix = os.path.splitext(audio.filename)[1]
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            temp_file_path = temp_file.name
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
        # 파일 크기 확인
        file_size = os.path.getsize(temp_file_path)
        if file_size == 0:
            if temp_file_path and os.path.exists(temp_file_path):
                os.remove(temp_file_path)
            raise CustomException(ResponseType.BAD_REQUEST, "빈 파일이 업로드되었습니다.")
        
        utils.log(f"파일이 {temp_file_path}에 저장되었습니다. 크기: {file_size} bytes", level=logging.DEBUG)
        
        # 백그라운드 태스크로 처리 작업 등록
        background_tasks.add_task(
            process_audio_in_background,
            temp_file_path=temp_file_path,
            file_name=audio.filename, 
            track_id=trackId,
            limit=limit,
            callback_url=callbackUrl
        )
        
        # 즉시 응답 반환
        return ApiResponse(payload={
            "message": "오디오 파일 처리가 백그라운드에서 시작되었습니다.",
            "file": audio.filename,
            "trackId": trackId,
            "callbackUrl": callbackUrl
        })
    
    except Exception as e:
        # 오류 발생 시 임시 파일 정리
        if temp_file_path and os.path.exists(temp_file_path):
            os.remove(temp_file_path)
        utils.log(f"비동기 오디오 처리 중 오류 발생: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"비동기 오디오 처리 중 오류 발생: {str(e)}")
