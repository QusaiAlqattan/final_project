package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.FolderDTO;
import org.example.final_project.model.Folder;
import org.example.final_project.repository.BranchRepository;
import org.example.final_project.repository.FolderRepository;
import org.example.final_project.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping()
    public ResponseEntity<List<FolderDTO>> fetchFolders() {
        return ResponseEntity.ok(folderService.getAllFolders());
    }

    @PostMapping()
    public ResponseEntity<FolderDTO> createFolder(@RequestBody FolderDTO folderDTO) {
        folderService.saveFolder(folderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(folderDTO);
    }
}
