package org.example.final_project.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DockerService {

    public String runCodeInDocker(String code, String language) {
        try {
            String dockerImage;
            String command;

            // Define the base directory for code storage (shared between containers)
            Path baseDir = Paths.get("/app/codes");
            Files.createDirectories(baseDir); // Ensure the directory exists

            // Determine the appropriate filename and Docker image
            Path filename;
            if (language.equalsIgnoreCase("java")) {
                dockerImage = "openjdk:11";
                filename = baseDir.resolve("Main.java");
                // Save the code to a file
                Files.writeString(filename, code);
                command = "javac /app/codes/Main.java && java -cp /app/codes Main";
            } else if (language.equalsIgnoreCase("javascript")) {
                dockerImage = "node:14";
                filename = baseDir.resolve("script.js");
                // Save the code to a file
                Files.writeString(filename, code);
                command = "node /app/codes/script.js";
            } else if (language.equalsIgnoreCase("python")) {
                dockerImage = "python";
                filename = baseDir.resolve("script.py");
                // Save the code to a file
                Files.writeString(filename, code);
                command = "python /app/codes/script.py";
            } else {
                return "Unsupported language";
            }

            // Run the Docker container with the appropriate command, using `--volumes-from`
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker",
                    "run",
                    "--rm",
                    "--volumes-from", "final_project_code-app-1", // Share the volume from the Spring Boot container
                    dockerImage,
                    "bash",
                    "-c",
                    command
            );
            Process process = processBuilder.start();

            // Capture the output
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read standard output
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Read standard error
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // Wait for the process to complete
            process.waitFor();

            // If there is error output, return that instead
            if (errorOutput.length() > 0) {
                return "Error running the code: " + errorOutput.toString();
            }

            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error running the code: " + e.getMessage();
        }
    }
}
