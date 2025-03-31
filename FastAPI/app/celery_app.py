from celery import Celery
import config # 설정 모듈 임포트

# Celery 앱 생성
app = Celery(
    'openl3_celery', # 앱 이름 지정
    broker=config.REDIS_URL, # 브로커 URL 설정
    backend=config.REDIS_URL, # 결과 백엔드 설정
    include=['app.services.openl3Tasks']
)

# 선택적 Celery 설정
app.conf.update(
    task_serializer='json', # 작업 직렬화 방식
    accept_content=['json'], # 허용할 콘텐츠 타입
    result_serializer='json', # 결과 직렬화 방식
    timezone='Asia/Seoul', # 시간대 설정
    enable_utc=True, # UTC 사용 여부
)