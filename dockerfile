# Use Maven image with JDK to build the app
FROM maven:3.9.10-eclipse-temurin-21-alpine AS build

# Set working directory inside the container
WORKDIR /app

# Copy Maven config files and source code
COPY pom.xml .
COPY src ./src

# Build the application (runs mvn clean package)
RUN mvn clean package

# Use official OpenJDK 21 runtime image
FROM eclipse-temurin:21-jre-jammy

# Set working directory inside the container
WORKDIR /app

# Copy your Spring Boot JAR into the container
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]