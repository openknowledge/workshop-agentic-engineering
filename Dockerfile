# tag::client-build[]
FROM maven:3.9.9-eclipse-temurin-21 AS client-build
RUN mkdir -p /usr/basisprojekt-client
WORKDIR /usr

# Cache node in a separate layer
COPY basisprojekt-client/pom.xml basisprojekt-client/pom.xml
RUN mvn -f basisprojekt-client/pom.xml initialize

# Cache the dependencies in a separate layer
COPY basisprojekt-client/package.json basisprojekt-client/package-lock.json basisprojekt-client/
RUN mvn -f basisprojekt-client/pom.xml generate-sources

# Build in a separate layer
COPY basisprojekt-client/ /usr/basisprojekt-client/
RUN mvn -f basisprojekt-client/pom.xml install -DskipLinting -DskipTests
# end::client-build[]

# tag::server-build[]
FROM maven:3.9.9-eclipse-temurin-21 AS mvn

# Copy client jar from client stage so the server can resolve the client dependency
RUN mkdir -p /root/.m2/repository/de/openknowledge
COPY --from=client-build /root/.m2/repository/de/openknowledge/ /root/.m2/repository/de/openknowledge/

# Cache server maven dependencies
COPY basisprojekt-server/pom.xml /usr/basisprojekt-server/
COPY basisprojekt-server/src/main/checkstyle/java.header.plain /usr/basisprojekt-server/src/main/checkstyle/java.header.plain
WORKDIR /usr/basisprojekt-server
RUN mvn dependency:resolve dependency:resolve-plugins dependency:go-offline spotless:check

# Build server
COPY basisprojekt-server/src src
RUN mvn -o -Dcheckstyle.skip -DskipTests clean install
# end::server-build[]

# tag::runtime[]
FROM eclipse-temurin:21-jre

# Copy executable server jar and start
RUN mkdir -p /usr/app
WORKDIR /usr/app
COPY --from=mvn /usr/basisprojekt-server/target/*.jar /usr/app/server.jar
ENTRYPOINT ["java", "-jar", "/usr/app/server.jar"]
# end::runtime[]
