import dotenv
import openl3
import numpy as np
import soundfile as sf
import os
import subprocess
import logging
from pymilvus import (
    connections,
    FieldSchema,
    CollectionSchema,
    DataType,
    Collection,
    utility
)
import config
import utils

class OpenL3Service:
    def __init__(self):
        self.milvus_host=config.MILVUS_HOST
        self.milvus_port=config.MILVUS_PORT
        self.collection_name=config.COLLECTION_NAME
        self.embedding_dim = config.EMBEDDING_DIM
        
        # Milvus 연결 설정
        connections.connect(host=self.milvus_host, port=self.milvus_port)
        
        # 컬렉션 존재 확인 및 생성
        self._init_collection()
        
    def _init_collection(self):
        """Milvus 컬렉션 초기화"""
        fields = [
            FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
            FieldSchema(name="track_id", dtype=DataType.INT64),  # 외부에서 받은 트랙 ID를 저장할 필드
            FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=self.embedding_dim),
        ]
        schema = CollectionSchema(fields, description="음악 임베딩")
        
        if not utility.has_collection(self.collection_name):
            self.collection = Collection(name=self.collection_name, schema=schema)
            # 인덱스 생성
            index_params = {
                "metric_type": "L2",
                "index_type": "IVF_FLAT",
                "params": {"nlist": 1024},
            }
            self.collection.create_index(field_name="embedding", index_params=index_params)
            print(f"컬렉션 '{self.collection_name}' 생성 완료.")
        else:
            self.collection = Collection(name=self.collection_name)
            print(f"컬렉션 '{self.collection_name}' 로드 완료.")
            
        self.collection.load()  # 메모리에 로드
    
    def extract_embedding(self, audio_path):
        """
        오디오 파일에서 OpenL3 임베딩 추출
        
        Args:
            audio_path: 오디오 파일 경로
            
        Returns:
            mean_embedding: 평균 임베딩 벡터
        """
        try:
            utils.log(f"오디오 파일 읽기 시도: {audio_path}", level=logging.DEBUG)
            
            # 파일 존재 및 내용 확인
            if not os.path.exists(audio_path):
                utils.log(f"파일이 존재하지 않음: {audio_path}", level=logging.ERROR)
                return None
                
            file_size = os.path.getsize(audio_path)
            if file_size == 0:
                utils.log(f"파일이 비어있음: {audio_path}", level=logging.ERROR)
                return None
                
            utils.log(f"파일 존재 확인 (크기: {file_size} 바이트)", level=logging.DEBUG)
            
            # 필요한 경우 오디오 파일을 표준 형식으로 변환
            try:
                # 직접 읽기 먼저 시도
                audio, sr = sf.read(audio_path)
                utils.log(f"오디오 파일 읽기 성공 (모양: {audio.shape}, 샘플레이트: {sr})", level=logging.DEBUG)
            except Exception as direct_read_error:
                # 직접 읽기 실패 시 ffmpeg로 변환 시도
                utils.log(f"직접 읽기 실패: {str(direct_read_error)}. ffmpeg로 변환 시도 중.", level=logging.WARNING)
                
                # 변환된 오디오를 위한 임시 파일 생성
                converted_path = f"{audio_path}_converted.wav"
                try:
                    # ffmpeg로 표준 WAV 형식으로 변환
                    cmd = ["ffmpeg", "-y", "-i", audio_path, "-acodec", "pcm_s16le", "-ar", "44100", "-ac", "2", converted_path]
                    utils.log(f"ffmpeg 명령어 실행: {' '.join(cmd)}", level=logging.DEBUG)
                    
                    result = subprocess.run(cmd, capture_output=True, text=True)
                    if result.returncode != 0:
                        utils.log(f"FFmpeg 변환 실패: {result.stderr}", level=logging.ERROR)
                        return None
                    
                    # 변환된 파일 읽기
                    audio_path = converted_path
                    audio, sr = sf.read(audio_path)
                    utils.log(f"변환된 오디오 읽기 성공 (모양: {audio.shape}, 샘플레이트: {sr})", level=logging.DEBUG)
                except Exception as conversion_error:
                    utils.log(f"변환 및 읽기 실패: {str(conversion_error)}", level=logging.ERROR)
                    return None
                finally:
                    # 변환된 파일 존재 시 정리
                    if os.path.exists(converted_path):
                        try:
                            os.remove(converted_path)
                        except:
                            pass
            
            # OpenL3 임베딩 추출
            utils.log(f"OpenL3 임베딩 추출 중...", level=logging.DEBUG)
            emb, ts = openl3.get_audio_embedding(
                audio,
                sr,
                input_repr="mel256",
                content_type="music",
                embedding_size=self.embedding_dim,
            )
            
            # 평균 임베딩 계산
            mean_emb = np.mean(emb, axis=0)
            utils.log(f"임베딩 추출 성공 (모양: {emb.shape})", level=logging.DEBUG)
            
            return mean_emb
        except Exception as e:
            utils.log(f"임베딩 추출 오류: {str(e)}", level=logging.ERROR)
            import traceback
            utils.log(traceback.format_exc(), level=logging.ERROR)
            return None
    
    def store_embedding(self, embedding, track_id=None):
        """
        임베딩을 Milvus에 저장
        
        Args:
            embedding: 저장할 임베딩 벡터
            track_id: 외부에서 받은 트랙 ID (선택적)
            
        Returns:
            id: 저장된 임베딩의 Milvus ID (실패 시 None)
        """
        try:
            # 입력이 numpy 배열이면 리스트로 변환
            if isinstance(embedding, np.ndarray):
                embedding = embedding.tolist()
            
            # track_id가 없으면 -1 등의 기본값 설정
            if track_id is None:
                track_id = -1
            
            # Milvus에 삽입할 데이터 준비
            data = [
                [track_id],      # track_id 필드
                [embedding]      # embedding 필드
            ]
            
            # Milvus에 데이터 삽입
            insert_result = self.collection.insert(data)
            
            # 명시적으로 flush 호출하여 데이터를 즉시 조회 가능하게 함
            self.collection.flush()
            
            print(f"데이터 삽입 완료. 컬렉션 항목 수: {self.collection.num_entities}")
            
            # Milvus가 자동 생성한 ID 반환
            if insert_result.primary_keys:
                return insert_result.primary_keys[0]
            return None
        except Exception as e:
            print(f"임베딩 저장 오류: {e}")
            return None
    
    def process_audio_file(self, file_path, track_id=None):
        """
        오디오 파일 처리 및 Milvus 저장
        
        Args:
            file_path: 처리할 오디오 파일 경로
            track_id: 외부에서 받은 트랙 ID (선택적)
            
        Returns:
            id: 저장된 임베딩의 Milvus ID (실패 시 None)
        """
        # 임베딩 추출
        embedding = self.extract_embedding(file_path)
        if embedding is None:
            return None
            
        # 임베딩 저장 (track_id 전달)
        return self.store_embedding(embedding, track_id)
    
    def find_similar_by_track_id(self, track_id, limit=5):
        """
        외부 트랙 ID로 유사한 곡 검색 (유사도 순으로 상위 n개 반환)
        
        Args:
            track_id: 검색 기준이 되는 트랙 ID
            limit: 반환할 유사 곡 수 (기본값 5)
            
        Returns:
            list: 유사한 곡 목록 (track_id와 유사도 점수)
        """
        try:
            # 컬렉션 항목 수 확인
            count = self.collection.num_entities
            print(f"컬렉션 항목 수: {count}")
            
            if count <= 1:
                print("컬렉션에 충분한 데이터가 없습니다.")
                return []
            
            # 트랙 ID로 임베딩 검색
            result = self.collection.query(
                expr=f'track_id == {track_id}',
                output_fields=["embedding"],
                limit=1,
            )
            
            if not result:
                print(f"트랙 ID {track_id}에 해당하는 임베딩을 찾을 수 없습니다.")
                return []
            
            search_embedding = result[0]['embedding']
            print(f"임베딩을 찾았습니다. 크기: {len(search_embedding)}")
            
            # 임베딩 벡터로 유사 트랙 검색
            similar_songs = self.find_similar_by_embedding(search_embedding, limit, exclude_track_id=track_id)
            
            # 결과 가공 및 정렬
            processed_results = []
            for track in similar_songs:
                # L2 거리를 유사도 점수로 변환 (거리가 작을수록 유사도 높음)
                similarity_score = 1.0 / (1.0 + track['distance'])
                
                processed_results.append({
                    "track_id": track['track_id'],
                    "similarity": round(similarity_score, 4),  # 소수점 4자리까지 표시
                    "distance": round(track['distance'], 4)
                })
            
            # 유사도 기준 내림차순 정렬
            processed_results.sort(key=lambda x: x['similarity'], reverse=True)
            
            return processed_results[:limit]
            
        except Exception as e:
            print(f"유사 트랙 검색 중 오류 발생: {e}")
            import traceback
            traceback.print_exc()  # 상세 오류 추적
            return []
    
    def find_similar_by_embedding(self, embedding, limit=5, exclude_track_id=None):
        """
        임베딩 벡터로 유사한 오디오 임베딩 검색
        
        Args:
            embedding: 검색 기준 임베딩 벡터
            limit: 반환할 결과 수
            exclude_track_id: 결과에서 제외할 트랙 ID (선택적)
            
        Returns:
            list: 유사한 임베딩 목록 (ID, 트랙 ID, 유사도 거리)
        """
        # 입력이 numpy 배열이면 리스트로 변환
        if isinstance(embedding, np.ndarray):
            embedding = embedding.tolist()
            
        # 유사성 검색 수행
        search_params = {
            "metric_type": "L2",
            "params": {"nprobe": 16}
        }
        
        # 제외할 트랙 ID가 있으면 expr 조건 추가
        expr = None
        if exclude_track_id is not None:
            expr = f"track_id != {exclude_track_id}"
        
        search_results = self.collection.search(
            data=[embedding],
            anns_field="embedding",
            param=search_params,
            limit=limit,
            output_fields=["track_id"],  # 트랙 ID도 함께 반환
            expr=expr,
        )
        
        # 결과 처리
        similar_embeddings = []
        if search_results and len(search_results) > 0:
            for hit in search_results[0]:
                # entity 객체에서 track_id 필드 안전하게 추출
                try:
                    # PyMilvus 2.2.x 이상 버전
                    track_id = hit.entity["track_id"]
                except (KeyError, TypeError):
                    try:
                        # 대체 접근 방법
                        track_id = getattr(hit.entity, "track_id", -1)
                    except:
                        # 모든 방법이 실패하면 기본값 사용
                        track_id = -1
                
                similar_embeddings.append({
                    "id": hit.id,
                    "track_id": track_id,
                    "distance": hit.distance,
                })
            print(f"검색된 유사 임베딩 수: {len(similar_embeddings)}")
        else:
            print("유사한 임베딩을 찾지 못했습니다.")
        
        return similar_embeddings

# 사용 예시
if __name__ == "__main__":
    # 서비스 인스턴스 생성
    openl3_service = OpenL3Service()
    
    # 오디오 파일 처리
    test_file = "path/to/audio.wav"
    if os.path.exists(test_file):
        embedding_id = openl3_service.process_audio_file(test_file)
        print(f"저장된 임베딩 ID: {embedding_id}")
        
        if embedding_id:
            # 유사한 임베딩 검색
            similar = openl3_service.find_similar_by_id(embedding_id)
            print("유사한 임베딩:")
            for item in similar:
                print(f"- 트랙 ID: {item['track_id']}, 거리: {item['distance']}")

# 다른 모듈에서 import할 수 있도록 모듈 레벨에 인스턴스 생성
openl3_service = OpenL3Service()