package org.example.final_project.controller;

import org.example.final_project.model.File;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            File file = fileRepository.getById(Long.parseLong(fileId));
            file.setContent(content);
            fileRepository.save(file);  // Directly pass the content
            return ResponseEntity.ok("File saved successfully!");
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