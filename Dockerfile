# syntax=docker/dockerfile:1.7

# tag::client-build[]
FROM maven:3.9.9-eclipse-temurin-21 AS client-build
RUN mkdir -p /usr/basisprojekt-client
WORKDIR /usr

# Cache node in a separate layer
COPY basisprojekt-client/pom.xml basisprojekt-client/pom.xml
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml initialize

# Cache the npm dependencies (npm cache mount; node_modules itself remains in layer)
COPY basisprojekt-client/package.json basisprojekt-client/package-lock.json basisprojekt-client/
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    --mount=type=cache,target=/root/.npm,id=npm-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml generate-sources

# Build (only build-relevant sources)
COPY basisprojekt-client/src            /usr/basisprojekt-client/src
COPY basisprojekt-client/public         /usr/basisprojekt-client/public
COPY basisprojekt-client/index.html     /usr/basisprojekt-client/index.html
COPY basisprojekt-client/vite.config.ts /usr/basisprojekt-client/vite.config.ts
COPY basisprojekt-client/tsconfig.app.json  /usr/basisprojekt-client/tsconfig.app.json
COPY basisprojekt-client/tsconfig.node.json /usr/basisprojekt-client/tsconfig.node.json
COPY basisprojekt-client/.prettierrc     /usr/basisprojekt-client/.prettierrc
COPY basisprojekt-client/.prettierignore /usr/basisprojekt-client/.prettierignore
# Override tsconfig.json: drop the test project reference -- tests don't run in the container,
# and we don't copy `tests/` to keep the build layer cacheable when test files change.
RUN echo '{"files":[],"references":[{"path":"./tsconfig.app.json"},{"path":"./tsconfig.node.json"}]}' \
    > /usr/basisprojekt-client/tsconfig.json
# Cache mount on /usr/basisprojekt-client/target so the install output (client jar) lands
# in the layer AND the maven cache mount holds the downloaded deps. We mount the maven
# repo cache so re-downloads are avoided when pom.xml changes invalidate the install layer.
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    --mount=type=cache,target=/root/.npm,id=npm-cache,sharing=locked \
    mvn -f basisprojekt-client/pom.xml install -DskipLinting -DskipTests \
 && mkdir -p /export/.m2/repository/de/openknowledge \
 && cp -r /root/.m2/repository/de/openknowledge/. /export/.m2/repository/de/openknowledge/
# end::client-build[]

# tag::server-build[]
FROM maven:3.9.9-eclipse-temurin-21 AS mvn

ARG CLIENT_GROUP_PATH=de/openknowledge
ARG CLIENT_ARTIFACT=basisprojekt-client
ARG CLIENT_VERSION=0.0.1-SNAPSHOT

# Stub jar + dependency resolution run together in ONE RUN: a cache mount on
# /root/.m2/repository overlays anything the layer placed there, so the stub jar must
# be created inside the same shell context as the mvn invocation that consumes it.
# This lets BuildKit start the server stage in parallel with client-build (the COPY
# only depends on basisprojekt-client/pom.xml, not on the client-build output).
COPY basisprojekt-client/pom.xml /tmp/client-pom.xml
COPY basisprojekt-server/pom.xml /usr/basisprojekt-server/
COPY basisprojekt-server/src/main/checkstyle/java.header.plain /usr/basisprojekt-server/src/main/checkstyle/java.header.plain
WORKDIR /usr/basisprojekt-server
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    CLIENT_REPO_PATH=/root/.m2/repository/${CLIENT_GROUP_PATH}/${CLIENT_ARTIFACT}/${CLIENT_VERSION} \
 && mkdir -p ${CLIENT_REPO_PATH} \
 && cp /tmp/client-pom.xml ${CLIENT_REPO_PATH}/${CLIENT_ARTIFACT}-${CLIENT_VERSION}.pom \
 && (cd /tmp && jar cf ${CLIENT_REPO_PATH}/${CLIENT_ARTIFACT}-${CLIENT_VERSION}.jar client-pom.xml) \
 && mvn dependency:resolve dependency:resolve-plugins dependency:go-offline spotless:check

# Stage the real client jar to a layer location -- a subsequent cache mount on
# /root/.m2/repository would otherwise hide a direct COPY into the maven repo.
COPY --from=client-build /export/.m2/repository/${CLIENT_GROUP_PATH}/ /tmp/client-repo/${CLIENT_GROUP_PATH}/

# Build server (only main sources -- tests are skipped anyway). Inside the cache mount,
# copy the real client jar over the stub jar, then run the offline install.
COPY basisprojekt-server/src/main src/main
RUN --mount=type=cache,target=/root/.m2/repository,id=mvn-cache,sharing=locked \
    mkdir -p /root/.m2/repository/${CLIENT_GROUP_PATH} \
 && cp -r /tmp/client-repo/${CLIENT_GROUP_PATH}/. /root/.m2/repository/${CLIENT_GROUP_PATH}/ \
 && mvn -o -Dcheckstyle.skip -DskipTests clean install
# end::server-build[]

# tag::runtime[]
FROM eclipse-temurin:21-jre

# Copy executable server jar and start
RUN mkdir -p /usr/app
WORKDIR /usr/app
COPY --from=mvn /usr/basisprojekt-server/target/*.jar /usr/app/server.jar
ENTRYPOINT ["java", "-jar", "/usr/app/server.jar"]
# end::runtime[]
