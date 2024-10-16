package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.FolderDTO;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class RESTFolderController {

    private final FolderRepository folderRepository;
    private final BranchRepository branchRepository;
    private final FolderService folderService;

    @Autowired
    public RESTFolderController(FolderRepository folderRepository, BranchRepository branchRepository, FolderService folderService) {
        this.folderRepository = folderRepository;
        this.branchRepository = branchRepository;
        this.folderService = folderService;
    }

    @GetMapping("{branchId}")
    public ResponseEntity<List<FolderDTO>> fetchFolders(@PathVariable Long branchId) {
        return ResponseEntity.ok(folderService.getFolders(branchId));
    }

    @PostMapping("{branchId}")
    public ResponseEntity<FolderDTO> createFolder(@RequestBody FolderDTO folderDTO, @PathVariable Long branchId) {
        folderService.saveFolder(folderDTO, branchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDTO);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolderById(folderId);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
