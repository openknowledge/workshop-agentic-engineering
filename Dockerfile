# syntax=docker/dockerfile:1.7

# tag::client-build[]
# ============================================================================
# Stage 1: Build Frontend (React/TypeScript/Vite)
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS client-build

RUN mkdir -p /usr/basisprojekt-client
WORKDIR /usr

# Cache node in a separate layer
COPY basisprojekt-client/pom.xml basisprojekt-client/pom.xml
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml initialize

# Cache npm dependencies (npm cache mount; node_modules itself remains in layer)
COPY basisprojekt-client/package.json basisprojekt-client/package-lock.json basisprojekt-client/
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    --mount=type=cache,target=/root/.npm,id=npm-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml generate-sources

# Copy build-relevant source files
COPY basisprojekt-client/src                    /usr/basisprojekt-client/src
COPY basisprojekt-client/public                 /usr/basisprojekt-client/public
COPY basisprojekt-client/index.html             /usr/basisprojekt-client/index.html
COPY basisprojekt-client/vite.config.ts         /usr/basisprojekt-client/vite.config.ts
COPY basisprojekt-client/tsconfig.app.json      /usr/basisprojekt-client/tsconfig.app.json
COPY basisprojekt-client/tsconfig.node.json     /usr/basisprojekt-client/tsconfig.node.json
COPY basisprojekt-client/.prettierrc            /usr/basisprojekt-client/.prettierrc
COPY basisprojekt-client/.prettierignore        /usr/basisprojekt-client/.prettierignore

# Override tsconfig.json: drop test project reference (tests don't run in container)
# We don't copy `tests/` to keep the build layer cacheable when test files change
RUN echo '{"files":[],"references":[{"path":"./tsconfig.app.json"},{"path":"./tsconfig.node.json"}]}' \
    > /usr/basisprojekt-client/tsconfig.json

# Build client and export to /export for next stage
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    --mount=type=cache,target=/root/.npm,id=npm-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml install -DskipLinting -DskipTests \
    && mkdir -p /export/.m2/repository/de/openknowledge \
    && cp -r /root/.m2/repository/de/openknowledge/. /export/.m2/repository/de/openknowledge/
# end::client-build[]

# tag::server-build[]
# ============================================================================
# Stage 2: Build Backend (Spring Boot)
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS mvn

ARG CLIENT_GROUP_PATH=de/openknowledge
ARG CLIENT_ARTIFACT=basisprojekt-client
ARG CLIENT_VERSION=0.0.1-SNAPSHOT

WORKDIR /usr/basisprojekt-server

# Copy server POM and checkstyle config for dependency resolution
COPY basisprojekt-client/pom.xml /tmp/client-pom.xml
COPY basisprojekt-server/pom.xml /usr/basisprojekt-server/
COPY basisprojekt-server/src/main/checkstyle/java.header.plain /usr/basisprojekt-server/src/main/checkstyle/java.header.plain

# Create stub client JAR and resolve dependencies in ONE RUN
# (cache mount overlays layer content, so stub must be created in same context)
# This allows BuildKit to parallelize client-build and server dependency resolution
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    CLIENT_REPO_PATH=/root/.m2/repository/${CLIENT_GROUP_PATH}/${CLIENT_ARTIFACT}/${CLIENT_VERSION} \
    && mkdir -p ${CLIENT_REPO_PATH} \
    && cp /tmp/client-pom.xml ${CLIENT_REPO_PATH}/${CLIENT_ARTIFACT}-${CLIENT_VERSION}.pom \
    && (cd /tmp && jar cf ${CLIENT_REPO_PATH}/${CLIENT_ARTIFACT}-${CLIENT_VERSION}.jar client-pom.xml) \
    && mvn dependency:resolve dependency:resolve-plugins dependency:go-offline spotless:check

# tag::download-otel-agent[]
# Download OpenTelemetry Java agent (pinned version)
RUN mkdir -p /opt/opentelemetry \
    && curl -L -o /opt/opentelemetry/opentelemetry-javaagent.jar \
        https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.25.0/opentelemetry-javaagent.jar \
    && chmod a+r /opt/opentelemetry/opentelemetry-javaagent.jar
# end::download-otel-agent[]

# Stage real client JAR from client-build stage
# (cache mount would hide direct COPY into maven repo, so stage to /tmp first)
COPY --from=client-build /export/.m2/repository/${CLIENT_GROUP_PATH}/ /tmp/client-repo/${CLIENT_GROUP_PATH}/

# Build server: copy real client JAR over stub, then run offline install
COPY basisprojekt-server/src/main src/main
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    mkdir -p /root/.m2/repository/${CLIENT_GROUP_PATH} \
    && cp -r /tmp/client-repo/${CLIENT_GROUP_PATH}/. /root/.m2/repository/${CLIENT_GROUP_PATH}/ \
    && mvn -o -Dcheckstyle.skip -DskipTests clean install \
    && chmod -R u+x /usr/basisprojekt-server/target/*.jar
# end::server-build[]

# tag::runtime[]
# ============================================================================
# Stage 3: Runtime (Minimal JRE)
# ============================================================================
<<<<<<< HEAD
FROM eclipse-temurin:21-jre-ubi10-minimal
=======
FROM eclipse-temurin:25-jre-ubi10-minimal
>>>>>>> 8740e8a (Initial setup)

WORKDIR /usr/app

# Copy server JAR and OpenTelemetry agent from build stage
COPY --from=mvn /usr/basisprojekt-server/target/*.jar /usr/app/server.jar
COPY --from=mvn /opt/opentelemetry/opentelemetry-javaagent.jar /opt/opentelemetry/opentelemetry-javaagent.jar

ENTRYPOINT ["java", "-javaagent:/opt/opentelemetry/opentelemetry-javaagent.jar", "-jar", "/usr/app/server.jar"]
# end::runtime[]
