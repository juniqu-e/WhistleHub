from fastapi import APIRouter, UploadFile, File, Form, Query, Path, HTTPException, status
import config
import logging
import utils
from common import ApiResponse, ResponseType
from common.exceptions import CustomException
import os
import tempfile
from typing import List, Optional

from app.services.UseOpenl3 import OpenL3Service

router = APIRouter(prefix=f"{config.API_BASE_URL}/track", tags=["audio"])

# OpenL3Service 인스턴스 생성
openl3_service = OpenL3Service()

# 트랙 ID를 Form 파라미터로 받는 API 예시
@router.post(
    "/",
    summary="오디오 파일 처리 API",
    description="오디오 파일을 업로드하고 임베딩을 추출하여 Milvus에 저장합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def process_audio(
    audio: UploadFile = File(..., description="처리할 오디오 파일"),
    track_id: Optional[int] = Form(None, description="외부 트랙 ID (선택적)")
):
    """오디오 파일 처리 API"""
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출 및 저장 (track_id 전달)
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

@router.get(
    "/similar/id/{track_id}",
    summary="ID로 유사한 트랙 검색 API",
    description="ID를 기준으로 유사한 오디오 트랙을 검색합니다",
    response_model=ApiResponse[List[dict]]
)
@utils.logger()
async def find_similar_by_track_id(
    track_id: int = Path(..., description="검색 기준 ID"),
    limit: int = Query(5, description="반환할 결과 수", gt=0, le=100)
):
    """ID로 유사한 트랙 검색 API"""
    try:
        similar_tracks = openl3_service.find_similar_by_track_id(track_id, limit)
        return ApiResponse(payload=similar_tracks)
    
    except Exception as e:
        utils.log(f"Error finding similar tracks by ID: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"유사 트랙 검색 중 오류 발생: {str(e)}")

@router.post(
    "/similar/embedding",
    summary="임베딩으로 유사한 트랙 검색 API",
    description="임베딩 벡터를 기준으로 유사한 오디오 트랙을 검색합니다",
    response_model=ApiResponse[List[dict]]
)
@utils.logger()
async def find_similar_by_embedding(
    embedding: List[float],
    limit: int = Query(5, description="반환할 결과 수", gt=0, le=100)
):
    """임베딩으로 유사한 트랙 검색 API"""
    try:
        if len(embedding) != openl3_service.EMBEDDING_DIM:
            raise CustomException(
                ResponseType.BAD_REQUEST, 
                f"임베딩 벡터의 차원이 일치하지 않습니다. 예상: {openl3_service.EMBEDDING_DIM}, 실제: {len(embedding)}"
            )
            
        similar_tracks = openl3_service.find_similar_by_embedding(embedding, limit)
        return ApiResponse(payload=similar_tracks)
    
    except Exception as e:
        utils.log(f"Error finding similar tracks by embedding: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"유사 트랙 검색 중 오류 발생: {str(e)}")

@router.post(
    "/upload-and-find-similar",
    summary="오디오 업로드 및 유사 트랙 검색 API",
    description="오디오 파일을 업로드하고 유사한 트랙을 바로 검색합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def upload_and_find_similar(
    audio: UploadFile = File(..., description="검색 기준 오디오 파일"),
    limit: int = Form(5, description="반환할 결과 수", gt=0, le=100)
):
    """오디오 업로드 및 유사 트랙 검색 API"""
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출
            embedding = openl3_service.extract_embedding(temp_file.name)
            
            if embedding is None:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")
            
            # 유사한 트랙 검색
            similar_tracks = openl3_service.find_similar_by_embedding(embedding, limit)
            
            return ApiResponse(payload={
                "filename": audio.filename,
                "similar_tracks": similar_tracks
            })
        
        except Exception as e:
            utils.log(f"Error processing audio and finding similar tracks: {str(e)}", level=logging.ERROR)
            raise CustomException(ResponseType.SERVER_ERROR, f"오디오 처리 중 오류 발생: {str(e)}")
        
        finally:
            # 임시 파일 삭제
            if os.path.exists(temp_file.name):
                os.remove(temp_file.name)

@router.get(
    "/embedding/{id}",
    summary="ID로 임베딩 조회 API",
    description="ID를 기준으로 임베딩 벡터를 조회합니다",
    response_model=ApiResponse[List[float]]
)
@utils.logger()
async def get_embedding_by_id(
    id: int = Path(..., description="조회할 임베딩 ID")
):
    """ID로 임베딩 조회 API"""
    try:
        embedding = openl3_service.get_embedding_by_id(id)
        
        if embedding is None:
            raise CustomException(ResponseType.NOT_FOUND_PAGE, f"ID {id}에 해당하는 임베딩을 찾을 수 없습니다")
            
        return ApiResponse(payload=embedding.tolist())
    
    except CustomException:
        # 이미 처리된 예외는 그대로 전달
        raise
    except Exception as e:
        utils.log(f"Error getting embedding by ID: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"임베딩 조회 중 오류 발생: {str(e)}")

@router.post(
    "/batch",
    summary="다중 오디오 파일 처리 API",
    description="여러 오디오 파일 경로를 받아 일괄 처리합니다",
    response_model=ApiResponse[dict]
)
@utils.logger()
async def batch_process_audio_files(
    file_paths: List[str]
):
    """다중 오디오 파일 처리 API"""
    try:
        results = openl3_service.batch_process_audio_files(file_paths)
        
        # 실패한 파일 개수 계산
        failed_count = sum(1 for id in results.values() if id is None)
        success_count = len(results) - failed_count
        
        return ApiResponse(payload={
            "message": f"{len(results)}개 파일 중 {success_count}개 처리 성공, {failed_count}개 실패",
            "results": results
        })
    
    except Exception as e:
        utils.log(f"Error batch processing audio files: {str(e)}", level=logging.ERROR)
        raise CustomException(ResponseType.SERVER_ERROR, f"다중 오디오 파일 처리 중 오류 발생: {str(e)}")

@router.post(
    "/extract-without-store",
    summary="임베딩 추출 API (저장하지 않음)",
    description="오디오 파일을 업로드하고 임베딩을 추출만 하고 저장하지 않습니다",
    response_model=ApiResponse[List[float]]
)
@utils.logger()
async def extract_embedding_without_store(
    audio: UploadFile = File(..., description="처리할 오디오 파일")
):
    """임베딩 추출 API (저장하지 않음)"""
    # 임시 파일로 저장
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(audio.filename)[1]) as temp_file:
        try:
            # 파일 저장
            contents = await audio.read()
            temp_file.write(contents)
            temp_file.flush()
            
            # 임베딩 추출
            embedding = openl3_service.extract_embedding(temp_file.name)
            
            if embedding is None:
                raise CustomException(ResponseType.SERVER_ERROR, "오디오 파일 처리에 실패했습니다")
            
            return ApiResponse(payload=embedding.tolist())
        
        except Exception as e:
            utils.log(f"Error extracting embedding: {str(e)}", level=logging.ERROR)
            raise CustomException(ResponseType.SERVER_ERROR, f"임베딩 추출 중 오류 발생: {str(e)}")
        
        finally:
            # 임시 파일 삭제
            if os.path.exists(temp_file.name):
                os.remove(temp_file.name)
