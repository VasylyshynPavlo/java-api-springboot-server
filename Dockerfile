# Use an official Maven image with OpenJDK 17 to build the application (if 21 is not found)
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the project files to the working directory
COPY pom.xml .
COPY src ./src

# Build the application using Maven
RUN mvn clean package

# Use a JDK image to run the application
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Expose the correct port (default for Spring Boot is 8080, unless you configured otherwise)
EXPOSE 8082

# Copy the built jar file from the Maven build stage
COPY --from=build /app/target/MyAPI_SpringBoot_Java23_Maven-1.0-SNAPSHOT.jar ./app.jar

# Specify the command to run the application
CMD ["java", "-jar", "app.jar"]
