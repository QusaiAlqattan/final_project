package org.example.final_project.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class ContainerManager {
    private final Map<String, Process> runningContainers = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int TIMEOUT_MINUTES = 10; // Set your idle timeout (in minutes)

    public void startContainer(String containerName, String imageName) throws Exception {
        // Start the container if not already running
        if (!runningContainers.containsKey(containerName)) {
            // Run the Docker container with the appropriate command, using `--volumes-from`
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker",
                    "run",
                    "--name", containerName, // Give the container a name
                    "--volumes-from", "final_project_code-app-1", // Share the volume from the Spring Boot container
                    imageName,
                    "tail",
                    "-f",
                    "/dev/null"
            );
            Process process = processBuilder.start();
            runningContainers.put(containerName, process);

            // Schedule a task to stop the container after idle time
            scheduleContainerTermination(containerName);

        } else {
            // If the container is already running, reset the timer
            resetTimer(containerName);
        }
    }

    public void scheduleContainerTermination(String containerName) {
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
            stopContainer(containerName);
        }, TIMEOUT_MINUTES, TimeUnit.MINUTES); // Ensure this is set to minutes

        // Store the scheduled task
        scheduledTasks.put(containerName, scheduledTask);
    }

    public void resetTimer(String containerName) {
        // Cancel the existing scheduled task if it exists
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(containerName);
        if (scheduledTask != null) {
            scheduledTask.cancel(false); // Cancel the existing task
        }

        // Schedule a new termination task
        scheduleContainerTermination(containerName);
    }

    public void stopContainer(String containerName) {
        Process process = runningContainers.remove(containerName);
        if (process != null) {
            try {
                // Stop the container
                ProcessBuilder stopProcessBuilder = new ProcessBuilder("docker", "stop", containerName);
                stopProcessBuilder.start().waitFor();

                // Remove the container
                ProcessBuilder removeProcessBuilder = new ProcessBuilder("docker", "rm", containerName);
                removeProcessBuilder.start().waitFor();

                // Remove the scheduled task
                scheduledTasks.remove(containerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
