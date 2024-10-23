package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.File;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.service.FileService;
import org.example.final_project.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.example.final_project.repository.RoleRepository;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class RESTWebSocketController {

    private final FileRepository fileRepository;

    private final WebSocketService webSocketService;

    @Autowired
    public RESTWebSocketController(FileRepository fileRepository, WebSocketService webSocketService) {
        this.fileRepository = fileRepository;
        this.webSocketService = webSocketService;
    }

    @PostMapping("/save/{fileId}")
    public ResponseEntity<String> saveFile(@PathVariable String fileId, @RequestBody String content) {
        try {
            return ResponseEntity.ok(webSocketService.createNewFile(fileId, content));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file");
        }
    }

    @GetMapping("/get/{fileId}")
    public ResponseEntity<String> getFileContent(@PathVariable String fileId) {
        try {
            File file = fileRepository.getById(Long.parseLong(fileId));
            String content = file.getContent();
            return ResponseEntity.ok(content); // Return the content as plain text
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching file content");
        }
    }
}
