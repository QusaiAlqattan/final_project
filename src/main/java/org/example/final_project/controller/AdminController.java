//package org.example.final_project.controller;
//
//import org.example.final_project.model.Folder;
//import org.example.final_project.service.FolderService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/folders")
//public class AdminController {
//
//    private final FolderService folderService;
//
//    @Autowired
//    public FolderController(FolderService folderService) {
//        this.folderService = folderService;
//    }
//
//    @GetMapping
//    public List<Folder> getAllFolders() {
//        return folderService.getAllFolders();
//    }
//
//    @PostMapping
//    public Folder createFolder(@RequestBody Folder folder) {
//        return folderService.saveFolder(folder);
//    }
//}
