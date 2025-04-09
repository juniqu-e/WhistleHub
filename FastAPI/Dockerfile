# -------------------------------
# Builder 단계: 의존성 설치
# -------------------------------
FROM python:3.11.11-slim as builder

WORKDIR /FastAPI/

# requirements.txt만 먼저 복사 - 이 파일이 변경되지 않으면 pip install은 캐싱됨
COPY ./requirements.txt .
RUN pip install --upgrade pip && pip install -r ./requirements.txt

# -------------------------------
# 최종 이미지 단계
# -------------------------------
FROM python:3.11-slim

WORKDIR /FastAPI/

# 시스템 패키지 설치
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

# builder에서 패키지 복사
COPY --from=builder /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# 설정 파일과 로그 파일 복사
COPY ./log_config.yaml ./log_config.yaml
COPY config/ ./config/
COPY log/ ./log/

# 소스 코드 복사 (자주 변경되는 파일들은 마지막에)
COPY app/ ./app/
COPY utils/ ./utils/
COPY common/ ./common/
COPY notebooks/ ./notebooks/

# PYTHONPATH 설정
ENV PYTHONPATH="/FastAPI"
RUN echo "/FastAPI" > /usr/local/lib/python3.11/site-packages/app.pth

# CMD 명령어
CMD ["sh", "-c", "python -m uvicorn app.main:app --host ${FAST_API_HOST} --port ${FAST_API_PORT}"]