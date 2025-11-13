FROM maven:3.9.11 AS builder
WORKDIR /application
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn clean install -Dmaven.test.skip

FROM bellsoft/liberica-openjre-alpine:25-cds AS layers
WORKDIR /application
COPY --from=builder /application/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM bellsoft/liberica-openjre-alpine:25-cds
VOLUME /tmp
RUN adduser -S spring-user
USER spring-user

WORKDIR /application

COPY --from=layers /application/extracted/dependencies/ ./
COPY --from=layers /application/extracted/spring-boot-loader/ ./
COPY --from=layers /application/extracted/snapshot-dependencies/ ./
COPY --from=layers /application/extracted/application/ ./

ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"

ENTRYPOINT java \
    $JAVA_ERROR_FILE_OPTS \
    -jar app.jar
