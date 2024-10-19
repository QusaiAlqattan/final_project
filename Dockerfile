# Step 1: Use an official OpenJDK image as the base image
FROM openjdk:22-jdk-slim

# Step 2: Install Docker CLI
RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

# Step 3: Set the working directory inside the container
WORKDIR /app

# Step 4: Copy the JAR file from the host machine to the container
COPY target/final_project-0.0.1-SNAPSHOT.jar /app/application.jar

# Step 5: Expose the port your Spring Boot app listens on (default is 8080)
EXPOSE 8080

# Step 6: Run the application
CMD ["java", "-jar", "/app/application.jar"]
