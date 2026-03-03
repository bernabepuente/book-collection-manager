# Stage 1 – build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first so dependency layer can be cached separately
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2 – runtime (slim JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
