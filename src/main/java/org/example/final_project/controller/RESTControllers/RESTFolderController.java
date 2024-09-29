package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.Folder;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class RESTFolderController {

    private final FolderRepository folderRepository;

    @Autowired
    public RESTFolderController(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @GetMapping()
    public List<Folder> fetchFolders() {
        List<Folder> folders = folderRepository.findAllWithContainerName();
        System.out.println("1111111111111111111111111111111111111111");
        System.out.println(folders.get(0).getContainerName());
        return folders; // This will be serialized to JSON automatically by Spring
    }

    @PostMapping
    public Folder createFolder(@RequestBody Folder folder) {
        return folderRepository.save(folder);
    }
}
