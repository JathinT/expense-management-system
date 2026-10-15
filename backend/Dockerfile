## Dockerfile for building and running the backend on Java 17
# Multi-stage build: use a Maven image to build (and run tests), then a smaller JRE image to run the jar.

FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom and sources and build the application. Tests run during `mvn package`.
COPY pom.xml .
COPY src ./src

# Use a non-interactive, batch mode build. This will run the project's tests as part of the package phase.
RUN mvn -B package

# Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage. Use wildcard to pick the generated jar.
COPY --from=build /workspace/target/*.jar app.jar

# Default port used by Render is provided in the PORT environment variable. Provide a default for local runs.
ENV PORT=8080

# Expose the default port to document runtime behavior. The application will use the PORT env var via the command below.
EXPOSE 8080

# Run the Spring Boot jar and configure the server port from the PORT environment variable.
# This ensures the application listens on the port supplied by the hosting platform (e.g. Render).
CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
