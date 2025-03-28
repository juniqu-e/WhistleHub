# -------------------------------
# Builder 단계: 의존성 설치
# -------------------------------
FROM python:3.11.11-slim as builder

WORKDIR /FastAPI/

COPY ./requirements.txt .
COPY log/ ./log/
RUN pip install --upgrade pip && pip install -r ./requirements.txt
# -------------------------------
# 최종 이미지 단계
# -------------------------------
FROM python:3.11-slim

WORKDIR /FastAPI/

# Install ffmpeg with full codec support
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ffmpeg \
    libsndfile1 \
    libavcodec-extra \
    libavformat-extra \
    libavutil-dev \
    libavfilter-dev \
    libgfortran5 && \
    rm -rf /var/lib/apt/lists/*

# builder 단계에서 설치된 패키지를 그대로 복사
COPY --from=builder /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages

# 소스 코드 복사
COPY config/ ./config/
COPY ./log_config.yaml ./log_config.yaml
COPY app/ ./app/
COPY utils/ ./utils/
COPY common/ ./common/
COPY notebooks/ ./notebooks/

# PYTHONPATH 설정
ENV PYTHONPATH="/FastAPI"

# 시스템 전역에 프로젝트 경로 추가
RUN echo "/FastAPI" > /usr/local/lib/python3.11/site-packages/app.pth

# CMD 명령어 (환경변수 확장 적용)
CMD ["sh", "-c", "python -m uvicorn app.main:app --host ${FAST_API_HOST} --port ${FAST_API_PORT}"]