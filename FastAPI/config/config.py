"""
서버의 환경변수를 설정하는 파일
"""
import os
import yaml
from dotenv import load_dotenv

load_dotenv()

MILVUS_HOST = os.getenv("MILVUS_HOST")
MILVUS_PORT = os.getenv("MILVUS_PORT")
COLLECTION_NAME = os.getenv("COLLECTION_NAME")
EMBEDDING_DIM = int(os.getenv("EMBEDDING_DIM"))
FAST_API_HOST = os.getenv("FAST_API_HOST")
FAST_API_PORT = int(os.getenv("FAST_API_PORT"))
API_BASE_URL = os.getenv("API_BASE_URL")
JWT_TOKEN = os.getenv("JWT_TOKEN")
REDIS_URL = os.getenv("REDIS_URL")
SHARED_AUDIO_DIR = os.getenv("SHARED_AUDIO_DIR")
with open(os.getenv("LOG_CONFIG_FILE"), encoding="utf-8") as file:
    LOG_CONFIG = yaml.safe_load(file)
