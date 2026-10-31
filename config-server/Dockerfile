# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

# Shared Maven cache + download retries (survives flaky connections)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl config-server -am clean package -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=10 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dhttp.keepAlive=false \
    -Daether.connector.http.connectionMaxTtl=25

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/config-server/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
