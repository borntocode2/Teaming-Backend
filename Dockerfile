# syntax=docker/dockerfile:1.7
# 1) Build stage
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Gradle 캐시 경로 고정
ENV GRADLE_USER_HOME=/workspace/.gradle

# Gradle Wrapper와 설정 파일 먼저 복사 (의존성 캐시 최적화)
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./

RUN chmod +x gradlew

# 의존성만 미리 받아 캐시 (소스 없이도 동작하도록 실패 무시)
RUN --mount=type=cache,target=/workspace/.gradle \
    ./gradlew --no-daemon dependencies || true

# 소스 복사 후 빌드 (테스트 생략)
COPY src src
RUN --mount=type=cache,target=/workspace/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

# Spring Boot Layertools로 레이어 추출
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination /workspace/layers

# 2) Runtime stage
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# 보안상 비루트 사용자
RUN useradd -r -u 10001 appuser && chown -R appuser /app
USER appuser

# 기본 환경
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul

# Layertools 레이어를 개별 복사 (더 좋은 캐시 타격)
COPY --from=builder /workspace/layers/dependencies/          /app/
COPY --from=builder /workspace/layers/snapshot-dependencies/ /app/
COPY --from=builder /workspace/layers/spring-boot-loader/    /app/
COPY --from=builder /workspace/layers/application/           /app/

EXPOSE 8080
# 컨테이너 메모리 친화적 옵션
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Seoul"

# JarLauncher로 부팅
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
