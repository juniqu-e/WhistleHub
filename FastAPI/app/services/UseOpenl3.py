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
from app.models.response.SimilarityResetResponseDto import *
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
            # 악기 타입 필드 추가
            FieldSchema(name="has_record", dtype=DataType.BOOL),      # 0: Record
            FieldSchema(name="has_whistle", dtype=DataType.BOOL),     # 1: Whistle
            FieldSchema(name="has_acoustic_guitar", dtype=DataType.BOOL),  # 2: Acoustic Guitar
            FieldSchema(name="has_voice", dtype=DataType.BOOL),       # 3: Voice
            FieldSchema(name="has_drums", dtype=DataType.BOOL),       # 4: Drums
            FieldSchema(name="has_bass", dtype=DataType.BOOL),        # 5: Bass
            FieldSchema(name="has_electric_guitar", dtype=DataType.BOOL),  # 6: Electric Guitar
            FieldSchema(name="has_piano", dtype=DataType.BOOL),       # 7: Piano
            FieldSchema(name="has_synth", dtype=DataType.BOOL),       # 8: Synth
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
                hop_size=1
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
    
    def store_embedding(self, embedding, track_id=None, instrumentTypes=None):
        """
        임베딩을 Milvus에 저장
        
        Args:
            embedding: 저장할 임베딩 벡터
            track_id: 외부에서 받은 트랙 ID (선택적)
            instrumentTypes: 악기 종류 리스트 (선택적)
            
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
            
            # instrumentTypes가 없으면 기본값 설정 (모두 False)
            has_record = False
            has_whistle = False
            has_acoustic_guitar = False
            has_voice = False
            has_drums = False
            has_bass = False
            has_electric_guitar = False
            has_piano = False
            has_synth = False
            
            # instrumentTypes가 리스트로 제공된 경우, 해당 악기들을 True로 설정
            if instrumentTypes and isinstance(instrumentTypes, list):
                if 0 in instrumentTypes: has_record = True
                if 1 in instrumentTypes: has_whistle = True
                if 2 in instrumentTypes: has_acoustic_guitar = True
                if 3 in instrumentTypes: has_voice = True
                if 4 in instrumentTypes: has_drums = True
                if 5 in instrumentTypes: has_bass = True
                if 6 in instrumentTypes: has_electric_guitar = True
                if 7 in instrumentTypes: has_piano = True
                if 8 in instrumentTypes: has_synth = True
            
            # Milvus에 삽입할 데이터 준비
            data = [
                [track_id],      # track_id 필드
                [embedding],      # embedding 필드
                [has_record],     # has_record 필드
                [has_whistle],    # has_whistle 필드
                [has_acoustic_guitar],  # has_acoustic_guitar 필드
                [has_voice],      # has_voice 필드
                [has_drums],      # has_drums 필드
                [has_bass],       # has_bass 필드
                [has_electric_guitar],  # has_electric_guitar 필드
                [has_piano],      # has_piano 필드
                [has_synth]       # has_synth 필드
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
    
    def process_audio_file(self, file_path, track_id=None, instrumentTypes=None):
        """
        오디오 파일 처리 및 Milvus 저장
        
        Args:
            file_path: 처리할 오디오 파일 경로
            track_id: 외부에서 받은 트랙 ID (선택적)
            instrumentTypes: 악기 종류 리스트 (선택적)
            
        Returns:
            id: 저장된 임베딩의 Milvus ID (실패 시 None)
        """
        # 임베딩 추출
        embedding = self.extract_embedding(file_path)
        if embedding is None:
            return None
            
        # 임베딩 저장 (track_id와 instrumentTypes 전달)
        return self.store_embedding(embedding, track_id, instrumentTypes)
    
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
        
        # 충분한 결과를 얻기 위해 요청 limit보다 더 많은 결과를 가져옴
        # distance=0인 항목이 있을 수 있으므로 여유있게 설정
        search_limit = limit + 5
        
        search_results = self.collection.search(
            data=[embedding],
            anns_field="embedding",
            param=search_params,
            limit=search_limit,
            output_fields=["track_id"],  # 트랙 ID도 함께 반환
            expr=expr,
        )
        
        # 결과 처리
        similar_embeddings = []
        excluded_count = 0
        
        if search_results and len(search_results) > 0:
            for hit in search_results[0]:
                # entity 객체에서 track_id 필드 안전하게 추출
                try:
                    track_id = hit.entity["track_id"]
                except (KeyError, TypeError):
                    try:
                        # 대체 접근 방법
                        track_id = getattr(hit.entity, "track_id", -1)
                    except:
                        # 모든 방법이 실패하면 기본값 사용
                        track_id = -1
                
                # distance가 0인 경우(동일한 임베딩)는 결과에서 제외
                if hit.distance == 0:
                    excluded_count += 1
                    continue
                
                similar_embeddings.append({
                    "id": hit.id,
                    "track_id": track_id,
                    "distance": hit.distance,
                })
                
                # 원하는 개수만큼 결과를 얻으면 종료
                if len(similar_embeddings) >= limit:
                    break
            
            print(f"검색된 유사 임베딩 수: {len(similar_embeddings)} (제외된 동일 임베딩: {excluded_count}개)")
        else:
            print("유사한 임베딩을 찾지 못했습니다.")
        
        return similar_embeddings
    
    def find_similar_by_instrument_type(self, needInstrumentTypes, trackIds):
        """
        악기 종류에 따라 유사한 트랙 검색 - 모든 트랙에서 한 번에 가장 유사한 트랙 찾기
        
        Args:
            needInstrumentTypes: 필요한 악기 종류 리스트 (True인 악기 타입을 검색에 사용)
            trackIds: 기존 트랙 ID 리스트
            
        Returns:
            int: 추천 트랙 ID (없으면 None)
        """
        try:
            # 입력값 검증
            utils.log(f"요청 악기 타입: {needInstrumentTypes}, 요청 트랙: {trackIds}", level=logging.INFO)

            # 컬렉션 항목 수 확인
            count = self.collection.num_entities
            utils.log(f"컬렉션 항목 수: {count}", level=logging.INFO)
            
            if count <= 1:
                utils.log("컬렉션에 충분한 데이터가 없습니다.", level=logging.WARNING)
                return None
            
            # 1. 입력된 모든 트랙의 임베딩을 한 번에 가져오기
            trackIds_str = ", ".join(map(str, trackIds))
            source_embeds = self.collection.query(
                expr=f"track_id in [{trackIds_str}]",
                output_fields=["track_id", "embedding"],
                limit=len(trackIds)
            )
            
            if not source_embeds:
                utils.log(f"입력된 트랙 ID들에 대한 임베딩을 찾을 수 없습니다.", level=logging.WARNING)
                return None
            
            utils.log(f"{len(source_embeds)}개의 소스 트랙 임베딩을 찾았습니다.", level=logging.INFO)
            
            # 2. 필요한 악기 타입에 대한 조건 구성
            instrument_mapping = [
                ("has_record", "레코드"),
                ("has_whistle", "휘슬"),
                ("has_acoustic_guitar", "어쿠스틱 기타"),
                ("has_voice", "보이스"),
                ("has_drums", "드럼"),
                ("has_bass", "베이스"),
                ("has_electric_guitar", "일렉기타"),
                ("has_piano", "피아노"),
                ("has_synth", "신디")
            ]
            
            # 필요한 악기 종류 파악
            needed_instruments = []
            for instrument_idx in needInstrumentTypes:
                if 0 <= instrument_idx < len(instrument_mapping):
                    needed_instruments.append(instrument_mapping[instrument_idx])
                    utils.log(f"악기 요청 추가: {instrument_mapping[instrument_idx][1]}({instrument_mapping[instrument_idx][0]})", level=logging.INFO)
            
            # 로그에 요청받은 악기 출력
            if needed_instruments:
                instruments_str = ", ".join([name for _, name in needed_instruments])
                utils.log(f"요청된 악기 종류: {instruments_str}", level=logging.INFO)
            else:
                utils.log("요청된 악기 종류 없음, 모든 트랙 대상으로 검색합니다.", level=logging.INFO)
            
            # 악기 조건 구성 (OR 조건으로 - 주어진 악기 중 하나라도 있으면 매칭)
            instrument_condition = ""
            for field_name, _ in needed_instruments:
                if instrument_condition:
                    instrument_condition += " || "
                instrument_condition += f"{field_name} == true"
            
            # 3. 기존 트랙에서 제외 조건 추가
            exclude_condition = f"track_id not in [{trackIds_str}]"
            
            # 4. 최종 쿼리 조건 구성 (악기 조건이 있으면 AND로 추가)
            final_condition = exclude_condition
            if instrument_condition:
                final_condition = f"{exclude_condition} && ({instrument_condition})"
            
            utils.log(f"검색 조건: {final_condition}", level=logging.DEBUG)
            
            # 5. 각 소스 트랙 임베딩을 사용하여 유사한 트랙 찾기
            all_similar_tracks = []
            
            for source in source_embeds:
                source_embedding = source["embedding"]
                source_track_id = source["track_id"]
                
                # 임베딩 기반 유사도 검색
                search_params = {
                    "metric_type": "L2",
                    "params": {"nprobe": 16}
                }
                
                search_results = self.collection.search(
                    data=[source_embedding],
                    anns_field="embedding",
                    param=search_params,
                    limit=10,  # 각 트랙당 상위 10개 후보를 검색
                    output_fields=["track_id"],
                    expr=final_condition
                )
                
                # 검색 결과 처리
                if search_results and len(search_results[0]) > 0:
                    for hit in search_results[0]:
                        try:
                            track_id = hit.entity.get("track_id")
                            if not track_id or track_id in trackIds:
                                continue
                                
                            similarity_score = 1.0 / (1.0 + hit.distance)
                            
                            all_similar_tracks.append({
                                "track_id": track_id,
                                "similarity": similarity_score,
                                "source_track_id": source_track_id,
                                "distance": hit.distance
                            })
                        except Exception as e:
                            utils.log(f"결과 처리 중 오류: {e}", level=logging.ERROR)
            
            # 6. 중복 제거 및 유사도 기준으로 정렬
            if not all_similar_tracks:
                utils.log("조건에 맞는 유사한 트랙을 찾을 수 없습니다.", level=logging.WARNING)
                return None
            
            # 트랙 ID별로 가장 높은 유사도 점수 유지 (여러 소스에서 같은 트랙이 나올 경우)
            track_id_to_best_score = {}
            for track in all_similar_tracks:
                track_id = track["track_id"]
                similarity = track["similarity"]
                
                if track_id not in track_id_to_best_score or similarity > track_id_to_best_score[track_id]["similarity"]:
                    track_id_to_best_score[track_id] = track
            
            # 유사도 기준 내림차순 정렬하여 최적의 트랙 찾기
            best_tracks = sorted(
                track_id_to_best_score.values(), 
                key=lambda x: x["similarity"], 
                reverse=True
            )
            
            if best_tracks:
                best_track = best_tracks[0]
                utils.log(f"가장 적합한 트랙 ID: {best_track['track_id']}, 유사도: {best_track['similarity']:.4f}, 소스 트랙: {best_track['source_track_id']}", 
                        level=logging.INFO)
                return best_track['track_id']
                
            return None
                
        except Exception as e:
            utils.log(f"유사 트랙 검색 중 오류 발생: {e}", level=logging.ERROR)
            import traceback
            utils.log(traceback.format_exc(), level=logging.ERROR)
            return None
        
    def find_similar_by_track_ids(self, trackIds, limit=5):
        """
        여러 트랙 ID로 유사한 곡 검색 (유사도 순으로 상위 n개 반환)
        
        Args:
            trackIds: 검색 기준이 되는 트랙 ID 리스트
            limit: 반환할 유사 곡 수 (기본값 5)
            
        Returns:
            SimilarityResetResponseDto: 각 트랙 ID에 대한 유사도 점수
        """
        try:
            # 컬렉션 항목 수 확인
            count = self.collection.num_entities
            print(f"컬렉션 항목 수: {count}")
            
            if count <= 1:
                print("컬렉션에 충분한 데이터가 없습니다.")
                return SimilarityResetResponseDto(__root__={})
            
            # 결과를 저장할 딕셔너리 초기화
            result_dict = {}
            
            # 각 트랙 ID에 대해 유사한 트랙 검색
            for track_id in trackIds:
                # 개별 트랙에 대한 유사 트랙 검색
                similar_tracks = self.find_similar_by_track_id(track_id, limit)
                
                # 결과가 있으면 SimilarityResetResponse 형태로 변환
                if similar_tracks:
                    track_results = []
                    for track in similar_tracks:
                        track_results.append(
                            SimilarityResetResponse(
                                trackId=track["track_id"],
                                similarity=track["similarity"]
                            )
                        )
                    # 문자열 키로 결과 저장 (딕셔너리 키는 문자열이어야 함)
                    result_dict[str(track_id)] = track_results
                else:
                    # 결과가 없는 경우 빈 리스트 설정
                    result_dict[str(track_id)] = []
            
            # SimilarityResetResponseDto 객체 생성 및 반환
            return SimilarityResetResponseDto(root=result_dict)
            
        except Exception as e:
            print(f"유사 트랙 검색 중 오류 발생: {e}")
            import traceback
            traceback.print_exc()
            # 오류 발생 시 빈 결과 반환
            return SimilarityResetResponseDto(root={})


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