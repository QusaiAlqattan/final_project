package org.example.final_project.controller;

import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.example.final_project.service.FileService;
import org.example.final_project.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
