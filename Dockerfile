FROM gradle:9.8.0-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle/libs.versions.toml gradle/
COPY src ./src

# the dependencies live in a cache mount rather than in a warm-up layer, so they survive a change to
# the build files, which would throw the layer away
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon shadowJar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 1000 is the usual first user on a linux host, so a mounted personality file is readable even when
# only its owner may read it
RUN adduser -u 1000 -D -s /bin/sh aibot
USER aibot

COPY --from=builder /app/build/libs/*-all.jar aibot.jar

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
    CMD test $(( $(date +%s) - $(stat -c %Y /tmp/health 2>/dev/null || echo 0) )) -lt 90

ENTRYPOINT ["java", "-jar", "aibot.jar"]
