package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.FileDTO;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class RESTFileController {

    private final FileService fileService;

    @Autowired
    public RESTFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("{branchId}")
    public ResponseEntity<List<FileDTO>> getFiles(@PathVariable Long branchId) {
        return ResponseEntity.ok(fileService.getFiles(branchId));
    }

    @PostMapping("{branchId}")
    public ResponseEntity<FileDTO> createFile(@RequestBody FileDTO fileDTO, @PathVariable Long branchId) {
        fileService.createFile(fileDTO, branchId, "");
        return ResponseEntity.status(HttpStatus.CREATED).body(fileDTO);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFileById(fileId);  // Make sure to implement this in the service
        return ResponseEntity.noContent().build();
    }

}
