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

RUN adduser -u 10001 -D -s /bin/sh aibot && chown -R aibot:aibot /app

USER aibot

# the app refreshes /tmp/health only while its getUpdates loop keeps cycling, so a stale file means
# the bot stopped polling telegram — which a process-level check cannot see, since polling runs on a
# scheduled executor the jvm happily outlives
#
# `test` rather than `[ … ]`: a CMD starting with `[` is read as the JSON exec form first, and only
# falls back to a shell command once that fails to parse
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
    CMD test $(( $(date +%s) - $(stat -c %Y /tmp/health 2>/dev/null || echo 0) )) -lt 90

ENTRYPOINT ["java", "-jar", "aibot.jar"]
