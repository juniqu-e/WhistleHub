# import requests
# from requests.exceptions import RequestException
# import config
# import logging
# import utils
# from common import ApiResponse, ResponseType
# from common.exceptions import CustomException
# from typing import Optional
# import os

# from app.services.UseOpenl3 import openl3_service

# # 백그라운드에서 실행될 오디오 처리 함수
# def process_audio_in_background(temp_file_path: str, file_name: str, track_id: Optional[int], limit: int, callback_url: str):
    
#     try:
#         utils.log(f"openl3 오디오 처리 시작: {file_name}", level=logging.INFO)
        
#         # OpenL3로 임베딩 추출 및 Milvus에 저장
#         milvus_id = openl3_service.process_audio_file(temp_file_path, track_id)

#         if not(milvus_id):
#             utils.log(f"openl3 오디오 처리 실패: {file_name}", level=logging.ERROR)

#         try:
#             if (milvus_id):
#                 # 임베딩 저장 성공 시 유사한 트랙 검색
#                 result_data = openl3_service.find_similar_by_track_id(track_id, limit) if track_id else []
#                 response = requests.post(
#                     callback_url +"/" + str(track_id), 
#                     json=ApiResponse(payload=result_data).model_dump(),
#                     headers={"Content-Type": "application/json", "Authorization": "Bearer " + config.JWT_TOKEN}
#                 )
#                 utils.log(f"Callback이 {callback_url}에 전송됨, 상태: {response.status_code}", level=logging.INFO)
#         except RequestException as e:
#             utils.log(f"Callback 전송 실패: {str(e)}", level=logging.ERROR)

#     except Exception as e:
#         utils.log(f"openl3 오디오 처리 실패: {str(e)}", level=logging.ERROR)
        
#     finally:
#         # 파일 존재 확인 추가
#         if temp_file_path and os.path.exists(temp_file_path): # 조건 수정
#             try:
#                 os.remove(temp_file_path)
#                 utils.log(f"파일 삭제됨 (Background): {temp_file_path}", level=logging.INFO)
#             except OSError as e:
#                 utils.log(f"파일 삭제 실패 (Background): {temp_file_path}, error: {e}", level=logging.ERROR)
#         else:
#             utils.log(f"삭제할 파일 없음 (Background): {temp_file_path}", level=logging.WARNING)