# Stage 1: Build multi-module Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY sports-common/pom.xml sports-common/pom.xml
COPY sports-feature-auth/pom.xml sports-feature-auth/pom.xml
COPY sports-feature-player/pom.xml sports-feature-player/pom.xml
COPY sports-feature-academy/pom.xml sports-feature-academy/pom.xml
COPY sports-feature-club/pom.xml sports-feature-club/pom.xml
COPY sports-feature-ai/pom.xml sports-feature-ai/pom.xml
COPY sports-web/pom.xml sports-web/pom.xml
RUN mvn -B dependency:go-offline

COPY . .
RUN mvn -B -DskipTests package

# Stage 2: Minimal JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /workspace/sports-web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
