FROM maven:3.9-eclipse-temurin-21 AS build

ARG SERVICE
WORKDIR /build

COPY ${SERVICE}/pom.xml pom.xml
RUN mvn -B -q dependency:go-offline

COPY ${SERVICE}/src/main src/main
RUN mvn -B -q -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S fluxo \
    && adduser -S -G fluxo fluxo

WORKDIR /app
COPY --from=build --chown=fluxo:fluxo /build/target/*-2.0.0.jar app.jar
USER fluxo

ENV SERVER_ADDRESS=0.0.0.0 \
    PORT=8080 \
    TZ=America/Sao_Paulo \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=45s --retries=12 \
    CMD ["curl", "--fail", "--silent", "http://127.0.0.1:8080/actuator/health"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
