FROM gradle:9.0.0-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle/libs.versions.toml gradle/
RUN gradle shadowJar -x test --no-daemon
COPY src ./src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar aibot.jar
COPY config ./config

RUN adduser -u 10001 -D -s /bin/sh aibot && chown -R aibot:aibot /app

USER aibot

ENTRYPOINT ["java", "-jar", "aibot.jar"]
