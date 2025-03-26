from fastapi import APIRouter, UploadFile, File, Form, Query, Path, HTTPException, status
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
    track_id: Optional[int] = Form(None, description="RDB 트랙 ID")
):
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출 및 milvus에에 저장 (track_id 전달)
            milvus_id = openl3_service.process_audio_file(temp_file.name, track_id)
            
            if not milvus_id:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")
            
            return ApiResponse(payload={
                "id": milvus_id,  # Milvus 내부 ID
                "track_id": track_id,  # 외부 트랙 ID 
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
    "/similar/{track_id}",
    summary="RDB 트랙 ID로 유사한 트랙 검색 API",
    description="RDB 트랙 ID를 기준으로 유사한 오디오 트랙을 검색합니다",
    response_model=ApiResponse[List[dict]]
)
@utils.logger()
async def find_similar_by_track_id(
    track_id: int = Path(..., description="검색 기준 RDB 트랙 ID"),
    limit: int = Query(5, description="반환할 결과 수", gt=0, le=100)
):
    try:
        similar_tracks = openl3_service.find_similar_by_track_id(track_id, limit)
        return ApiResponse(payload=similar_tracks)
    
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
    track_id: Optional[int] = Form(None, description="RDB 트랙 ID"),
    limit: int = Form(5, description="반환할 결과 수", gt=0, le=100)
):
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출 및 milvus에에 저장 (track_id 전달)
            milvus_id = openl3_service.process_audio_file(temp_file.name, track_id)
            
            if not milvus_id:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")

            
            # 트랙 ID로 유사도 검색
            similar_tracks = openl3_service.find_similar_by_track_id(track_id, limit)
            
            return ApiResponse(payload={
                "processed_file": {
                    "milvus_id": milvus_id,
                    "track_id": track_id,
                    "filename": audio.filename
                },
                "similar_tracks": similar_tracks
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
    metadata: str = Form(..., description="JSON 형식의 파일 메타데이터 {파일명: track_id, 파일명: track_id, ...}")
):
    # 1. JSON 파싱, 파일명과 메타데이터 검증
    try:
        file_metadata = json.loads(metadata)
    except json.JSONDecodeError as e:
        utils.log(f"Invalid JSON metadata: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.BAD_REQUEST, f"올바르지 않은 JSON 형식: {str(e)}")

    uploaded_filenames = {file.filename for file in files}
    metadata_filenames = set(file_metadata.keys())
    
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
                    
                    # 메타데이터에서 track_id 찾기
                    track_id = file_metadata.get(file.filename)
                    if track_id is None:
                        utils.log(f"No track_id found for file: {file.filename}", level=logging.WARNING)
                        results[file.filename] = {
                            "milvus_id": None,
                            "track_id": None,
                            "error": "메타데이터에 track_id가 없습니다"
                        }
                        continue
                    
                    # 임베딩 추출 및 저장
                    milvus_id = openl3_service.process_audio_file(temp_file.name, track_id)
                    results[file.filename] = {
                        "milvus_id": milvus_id,
                        "track_id": track_id,
                        "status": "success" if milvus_id else "failed"
                    }
                    
                except Exception as file_error:
                    utils.log(f"Error processing file {file.filename}: {str(file_error)}", level=logging.ERROR)
                    results[file.filename] = {
                        "milvus_id": None,
                        "track_id": file_metadata.get(file.filename),
                        "error": str(file_error)
                    }
                
                finally:
                    # 임시 파일 삭제
                    if os.path.exists(temp_file.name):
                        os.remove(temp_file.name)
        
        # 결과 통계 계산
        failed_count = sum(1 for item in results.values() if item.get("milvus_id") is None)
        success_count = len(results) - failed_count
        
        return ApiResponse(payload={
            "message": f"{len(results)}개 파일 중 {success_count}개 처리 성공, {failed_count}개 실패",
            "results": results
        })
    
    except Exception as e:
        utils.log(f"Error batch processing audio files: {str(e)}", level=logging.ERROR)
        import traceback
        traceback.print_exc()
        raise CustomException(ResponseType.SERVER_ERROR, f"다중 오디오 파일 처리 중 오류 발생: {str(e)}")