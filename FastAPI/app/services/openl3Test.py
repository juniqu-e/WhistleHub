import os
import numpy as np
import openl3
import soundfile as sf
from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pymilvus import (
    connections,
    FieldSchema,
    CollectionSchema,
    DataType,
    Collection,
    utility
)

app = FastAPI(title="MusicEX API")

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Milvus 서버 연결 정보 (docker-compose.yml 설정에 따라 조정)
MILVUS_HOST = "milvus-standalone"  # 또는 Docker 컨테이너 이름 (e.g., 'milvus-standalone')
MILVUS_PORT = "19530"

connections.connect(host=MILVUS_HOST, port=MILVUS_PORT)

# 컬렉션 이름
COLLECTION_NAME = "music_embeddings"
# 임베딩 차원 (OpenL3의 embedding_size와 일치해야 함)
EMBEDDING_DIM = 512

# 컬렉션 스키마 정의
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
    FieldSchema(name="filename", dtype=DataType.VARCHAR, max_length=256),  # 파일 이름 저장
    FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=EMBEDDING_DIM),
]
schema = CollectionSchema(fields, description="Music embeddings")

# 컬렉션 존재 여부 확인 및 생성/로드
if not utility.has_collection(COLLECTION_NAME):
    collection = Collection(name=COLLECTION_NAME, schema=schema)
    # 인덱스 생성 (IVF_FLAT 권장, 필요에 따라 다른 인덱스 타입 사용)
    index_params = {
        "metric_type": "L2",  # 거리 측정 기준 (L2, IP 등)
        "index_type": "IVF_FLAT",  # 인덱스 타입
        "params": {"nlist": 1024},  # 인덱스 파라미터 (튜닝 필요)
    }
    collection.create_index(field_name="embedding", index_params=index_params)

    print(f"Collection '{COLLECTION_NAME}' created.")
else:
    collection = Collection(name=COLLECTION_NAME)
    print(f"Collection '{COLLECTION_NAME}' loaded.")

collection.load() # collection을 로드하여 메모리에 올림.

@app.get("/")
async def root():
    return {"message": "MusicEX API is running"}

@app.post("/process-audio")
async def process_audio(file: UploadFile = File(...)):
    # 업로드된 파일 저장
    file_path = f"temp_{file.filename}"
    with open(file_path, "wb") as buffer:
        buffer.write(await file.read())

    try:
        # 오디오 파일 로드
        audio, sr = sf.read(file_path)

        # OpenL3를 사용하여 임베딩 추출
        emb, ts = openl3.get_audio_embedding(
            audio,
            sr,
            input_repr="mel256",
            content_type="music",
            embedding_size=512,
        )

        # 임베딩 평균 계산
        mean_emb = np.mean(emb, axis=0).tolist()

        # Milvus에 삽입할 데이터 준비
        data = [
            [file.filename],   # filename (VARCHAR)
            [mean_emb],        # embedding (FLOAT_VECTOR)
        ]

        # Milvus에 데이터 삽입
        insert_result = collection.insert(data)
        
        # 삽입된 ID 반환 (선택 사항)
        # primary_keys = insert_result.primary_keys
        # print(f"Inserted IDs: {primary_keys}")
        

        return {"status": "success", "filename": file.filename}

    finally:
        # 임시 파일 삭제
        if os.path.exists(file_path):
            os.remove(file_path)

@app.get("/similar/{filename}")
async def get_similar(filename: str, limit: int = 5):
    # 1. filename을 기반으로 embedding vector를 가져옴
    result = collection.query(
        expr=f'filename == "{filename}"',  # 파일 이름으로 검색
        output_fields=["embedding"],  # 임베딩 필드만 가져옴
        limit=1,  # limit을 1로 설정하여 하나의 결과만 가져오도록 한다.
    )
    if not result:
        return {"error": f"File '{filename}' not found."}
    
    search_embedding = result[0]['embedding']

    # 2. Milvus에서 유사성 검색 수행
    search_params = {
        "metric_type": "L2",  # 삽입 시 사용한 거리 측정 기준과 동일해야 함
         "params": {"nprobe": 16}  # 검색 파라미터 (튜닝 필요, 높을수록 정확하지만 느려짐)
    }
    
    search_results = collection.search(
        data=[search_embedding],  # 검색할 임베딩
        anns_field="embedding",  # 임베딩 필드 이름
        param=search_params,
        limit=limit,  # 반환할 결과 수
        output_fields=["filename"],  # 반환할 필드 (파일 이름)
        expr=None,  # 추가적인 필터 조건 (선택 사항, 예: 특정 날짜 이후)
    )

    # 3. 검색 결과 정리 및 반환
    similar_tracks = []
    # search_results는 list of SearchResult 객체.
    if search_results: # search_results가 비어있지 않은지 확인
        for hit in search_results[0]: # 첫 번째 SearchResult에 접근 (query vector가 1개 이므로)
            similar_tracks.append({
                "filename": hit.entity.get("filename"), # entity에서 filename 가져오기
                "distance": hit.distance,  # 유사도 거리
                #"id": hit.id, # 필요하면 id도 가져올 수 있음
            })
        return {"similar_tracks": similar_tracks}
    else:
        return {"similar_tracks" : []}