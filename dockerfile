# Use the official maven image for building the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download all required dependencies
RUN mvn dependency:go-offline -B

# Copy the project source
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .
COPY mvnw.cmd .

# Package the application
RUN mvn package -DskipTests

# Use OpenJDK for running the application
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Create directory for file uploads
RUN mkdir -p /app/uploads

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8000

# Command to run the application
CMD ["java", "-jar", "app.jar"]