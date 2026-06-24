# Build from monorepo root: docker build -f project-service/Dockerfile -t project-service .
# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /workspace
COPY common-lib ./common-lib
RUN cd common-lib && mvn clean install -DskipTests -q
COPY project-service/pom.xml ./project-service/pom.xml
COPY project-service/src ./project-service/src
RUN cd project-service && mvn clean package -DskipTests -q
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/project-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
