package org.example.final_project.service;

import org.example.final_project.model.Folder;
import org.example.final_project.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    // Retrieve all folders
    public List<Folder> getAllFolders() {
        return folderRepository.findAll();
    }

    // Save a new folder
    public Folder saveFolder(Folder folder) {
        return folderRepository.save(folder);
    }
}
