# Employee Service Dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application
EXPOSE 8083
CMD ["java", "-jar", "target/employee-service-0.0.1-SNAPSHOT.jar"]