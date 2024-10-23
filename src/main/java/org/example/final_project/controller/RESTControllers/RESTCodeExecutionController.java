package org.example.final_project.controller.RESTControllers;

import org.example.final_project.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RESTCodeExecutionController {

    private final DockerService dockerService;

    @Autowired
    public RESTCodeExecutionController(DockerService dockerService){
        this.dockerService = dockerService;
    }

    @PostMapping("/run-code")
    public Map<String, String> runCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String language = payload.get("language");
        String fileId = payload.get("fileId");

        String output = dockerService.runCodeInDocker(code, language, fileId);
        return Map.of("output", output);
    }
}
