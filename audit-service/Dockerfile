# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

# Shared Maven cache + download retries (survives flaky connections)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl audit-service -am clean package -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=10 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dhttp.keepAlive=false \
    -Daether.connector.http.connectionMaxTtl=25

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/audit-service/target/*.jar app.jar

EXPOSE 8084

# Same time zone as the monolith (IST), so createdAt / fraud-check times match
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]
