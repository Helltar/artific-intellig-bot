FROM gradle:9.0.0-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle/libs.versions.toml gradle/

RUN gradle --no-daemon shadowJar

COPY src ./src
RUN gradle --no-daemon shadowJar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN adduser -u 10001 -D -s /bin/sh aibot
USER aibot

COPY --from=builder /app/build/libs/*-all.jar aibot.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
    CMD test $(( $(date +%s) - $(stat -c %Y /tmp/health 2>/dev/null || echo 0) )) -lt 90

ENTRYPOINT ["java", "-jar", "aibot.jar"]
