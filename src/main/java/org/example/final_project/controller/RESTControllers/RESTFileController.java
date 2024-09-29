package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.File;
import org.example.final_project.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class RESTFileController {

    private final FileRepository fileRepository;

    @Autowired
    public RESTFileController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @GetMapping
    public List<File> getAllFiles() {
        return fileRepository.findAllWithContainerName();
    }

    @PostMapping
    public File createFile(@RequestBody File file) {
        file.setTimestamp(LocalDateTime.now());
        file.setVersion("1");
        //TODO: set creator, after managing sessions
        return fileRepository.save(file);
    }
}
