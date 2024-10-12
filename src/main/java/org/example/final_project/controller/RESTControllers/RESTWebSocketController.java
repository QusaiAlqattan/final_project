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

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private WebSocketService webSocketService;

//    @GetMapping("/files/{fileId}/content")
//    public ResponseEntity<?> getFileContent(@PathVariable String fileId) {
//        // Fetch file content from your storage (file system, database, etc.)
//        File file = fileRepository.getById(Long.parseLong(fileId));
//        String content = file.getContent();
//
//        Map<String, String> response = new HashMap<>();
//        response.put("name", file.getName());  // You can adjust this based on your file metadata
//        response.put("content", content);
//        return ResponseEntity.ok(response);
//    }

    // Save file content
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

//    @PostMapping("/lock/acquire/{fileId}")
//    public ResponseEntity<Map<String, Boolean>> acquireLock(@PathVariable String fileId) {
//        Map<String, Boolean> response = new HashMap<>();
//        Boolean locked = webSocketService.acquireLock(fileId);
//        response.put("locked", locked);
//        return ResponseEntity.ok(response);
//    }

//    @PostMapping("/lock/release/{fileId}")
//    public ResponseEntity<Void> releaseLock(@PathVariable String fileId) {
//        webSocketService.releaseLock(fileId);
//        return ResponseEntity.noContent().build();
//    }

}
