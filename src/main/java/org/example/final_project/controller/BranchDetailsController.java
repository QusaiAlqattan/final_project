package org.example.final_project.controller;

import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.service.FileService;
import org.example.final_project.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/branch-detail")
public class BranchDetailsController {

    private final FolderService folderService;
    private final FileService fileService;

    @Autowired
    public BranchDetailsController(FolderService folderService, FileService fileService) {
        this.folderService = folderService;
        this.fileService = fileService;
    }

    @GetMapping("/{branchId}")
    public String openContentPage() {
        return "contents";
    }
//    // API to fetch folders for a specific branch
//    @GetMapping("/folders")
//    public ResponseEntity<List<Folder>> getFoldersByBranchId(@RequestParam("branchId") String branchId) {
//        List<Folder> folders = folderService.getFoldersByBranchId(branchId);
//        return ResponseEntity.ok(folders);
//    }
//
//    // API to fetch files for a specific branch
//    @GetMapping("/files")
//    public ResponseEntity<List<File>> getFilesByBranchId(@RequestParam("branchId") String branchId) {
//        List<File> files = fileService.getFilesByBranchId(branchId);
//        return ResponseEntity.ok(files);
//    }
}
