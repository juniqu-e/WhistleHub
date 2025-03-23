FROM openjdk:17-slim as build
WORKDIR /app

# 1단계: Gradle Wrapper 및 설정 파일만 먼저 복사
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
# 의존성 미리 다운로드 (의존성 관련 파일이 변경되지 않으면 이 레이어는 캐시 활용)
RUN ./gradlew --no-daemon dependencies

# 2단계: 나머지 소스코드 복사 후 전체 빌드 실행 (테스트는 제외)
COPY . .

RUN ./gradlew --no-daemon clean build -x test

# 최종 이미지 스테이지
FROM openjdk:17-slim
WORKDIR /app

RUN apt-get update && \
    apt-get install -y ffmpeg

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
