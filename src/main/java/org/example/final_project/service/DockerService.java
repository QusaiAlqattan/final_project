package org.example.final_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DockerService {

    @Autowired
    private ContainerManager containerManager;

    private final ReentrantLock lock = new ReentrantLock();

    public String runCodeInDocker(String code, String language, String fileId) {
        lock.lock();
        try {
            String dockerImage;
            String command;

            // Define the base directory for code storage (shared between containers)
            Path baseDir = Paths.get("/app/codes");
            Files.createDirectories(baseDir); // Ensure the directory exists

            // Determine the appropriate filename and Docker image
            Path filename;
            if (language.equalsIgnoreCase("java")) {
                dockerImage = "openjdk:22";
                filename = baseDir.resolve("Main.java");
                // Save the code to a file
                Files.writeString(filename, code);

                // Use a separate writable directory for .class files
                command = "mkdir -p /app/com && javac -d /app/com /app/codes/Main.java && java -cp /app/com Main";
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

            String containerName = dockerImage.replace(":", "") + fileId;

            // Start the container if not already running and run command
            containerManager.startContainer(containerName, dockerImage);

            // Wait until the container is running
            while (!isContainerRunning(containerName)) {
                Thread.sleep(1000); // Check every second
            }

            // Now execute the command
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "exec", containerName, "bash", "-c", command);
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
        }finally {
            lock.unlock();
        }
    }

    private boolean isContainerRunning(String containerName) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "inspect", "-f", "{{.State.Running}}", containerName);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        return "true".equals(line);
    }

}
