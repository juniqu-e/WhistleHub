# app/tasks.py
import os
import logging
import requests
from requests.exceptions import RequestException
from celery import chain

# Celery 앱 인스턴스 임포트
from app.celery_app import app

# 필요한 서비스 및 유틸리티 임포트
import utils
import config
from common import ApiResponse
from app.services.UseOpenl3 import openl3_service # 싱글톤 인스턴스 가정

@app.task(bind=True, name='tasks.process_audio') # bind=True는 self 인자를 통해 작업 컨텍스트 접근 허용
def task_process_audio(self, temp_file_path: str, track_id: int | None):
    """
    Celery 작업: OpenL3를 사용하여 오디오를 처리하고 Milvus에 저장합니다.
    다음 작업을 위해 track_id를 반환합니다 (필요하다면 Milvus ID 반환 가능).
    """
    utils.log(f"[Task {self.request.id}] 오디오 파일 처리 시작: {temp_file_path} (track_id: {track_id})", level=logging.INFO)
    try:
        # OpenL3로 임베딩 추출 및 Milvus에 저장
        # process_audio_file이 Milvus ID를 반환하거나 성공 여부를 확인한다고 가정
        # 다음 단계(유사도 검색)를 위해 원본 track_id가 필요하다고 가정
        milvus_id = openl3_service.process_audio_file(temp_file_path, track_id)

        if not milvus_id:
            utils.log(f"[Task {self.request.id}] 오디오 파일 처리 실패: {temp_file_path}", level=logging.ERROR)
            # 실패 처리 방식 결정: 오류 발생, 특정 값 반환 등
            # 오류를 발생시키면 체인이 중단될 수 있음 (처리하지 않는 한)
            # 여기서는 실패를 나타내기 위해 None을 반환하고 정리 작업은 허용
            return None # 실패 표시

        utils.log(f"[Task {self.request.id}] 오디오 처리 성공. Milvus ID: {milvus_id}, Track ID: {track_id}", level=logging.INFO)
        return track_id # 다음 작업으로 track_id 전달

    except Exception as e:
        utils.log(f"[Task {self.request.id}] 오디오 처리 중 오류 발생: {str(e)}", level=logging.ERROR)
        # Celery 설정에 따라 재시도/실패 처리를 위해 예외 발생
        raise e # 예외 다시 발생

@app.task(bind=True, name='tasks.find_similar')
def task_find_similar(self, prev_task_result: int | None, limit: int):
    """
    Celery 작업: track_id를 기반으로 유사한 트랙을 찾습니다.
    prev_task_result는 이전 작업의 반환값 (track_id 또는 None)입니다.
    유사한 트랙 목록을 반환합니다.
    """
    if prev_task_result is None:
         utils.log(f"[Task {self.request.id}] 이전 작업 실패로 유사도 검색 건너뜀.", level=logging.WARNING)
         return [] # 처리 실패 시 빈 목록 반환

    track_id = prev_task_result
    utils.log(f"[Task {self.request.id}] 유사 트랙 검색 시작 (track_id: {track_id}, limit: {limit})", level=logging.INFO)
    try:
        similar_tracks = openl3_service.find_similar_by_track_id(track_id, limit)
        utils.log(f"[Task {self.request.id}] {len(similar_tracks)}개의 유사 트랙 검색 완료.", level=logging.INFO)
        return similar_tracks # 다음 작업으로 결과 전달

    except Exception as e:
        utils.log(f"[Task {self.request.id}] 유사도 검색 중 오류 발생: {str(e)}", level=logging.ERROR)
        raise e # 예외 다시 발생

@app.task(bind=True, name='tasks.send_callback')
def task_send_callback(self, similar_tracks: list, callback_url: str, track_id: int | None):
    """
    Celery 작업: 결과를 콜백 URL로 전송합니다.
    similar_tracks는 task_find_similar의 결과입니다.
    """
    utils.log(f"[Task {self.request.id}] 콜백 준비 중: {callback_url} (track_id: {track_id})", level=logging.INFO)

    # 적절한 JSON 직렬화를 위해 model_dump 사용
    api_response_json = ApiResponse(payload=similar_tracks).model_dump(mode='json')

    try:
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {config.JWT_TOKEN}" # JWT_TOKEN 로드 확인
        }
        # track_id가 존재하고 콜백이 이를 URL에 필요로 하는 경우에만 추가
        target_url = f"{callback_url}/{track_id}" if track_id is not None else callback_url

        response = requests.post(
            target_url,
            json=api_response_json, # 직렬화된 JSON 전송
            headers=headers,
            timeout=30 # 타임아웃 추가
        )
        response.raise_for_status() # 잘못된 응답(4xx 또는 5xx) 시 HTTPError 발생

        utils.log(f"[Task {self.request.id}] 콜백 전송 성공: {target_url}. 상태 코드: {response.status_code}", level=logging.INFO)
        return {"status": response.status_code, "url": target_url} # 상태 반환

    except RequestException as e:
        utils.log(f"[Task {self.request.id}] 콜백 요청 실패: {str(e)}", level=logging.ERROR)
        # 재시도 로직 결정 또는 오류 발생
        raise e # 재시도를 유발하기 위해 예외 다시 발생

@app.task(bind=True, name='tasks.cleanup_file', ignore_result=True)
def task_cleanup_file(self, temp_file_path: str):
    utils.log(f"[Task {self.request.id}] 파일 정리 시작: {temp_file_path}", level=logging.INFO)
    try:
        # 파일 존재 확인 추가
        if temp_file_path and os.path.exists(temp_file_path):
            os.remove(temp_file_path)
            utils.log(f"[Task {self.request.id}] 파일 삭제 완료: {temp_file_path}", level=logging.INFO)
        else:
            utils.log(f"[Task {self.request.id}] 파일을 찾을 수 없거나 경로가 None입니다: {temp_file_path}", level=logging.WARNING)
    except OSError as e:
        utils.log(f"[Task {self.request.id}] 파일({temp_file_path}) 삭제 중 오류 발생: {str(e)}", level=logging.ERROR)