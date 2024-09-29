package org.example.final_project.repository;

import org.example.final_project.model.Folder;

import java.util.List;

public interface CustomFolderRepository {
    List<Folder> findAllWithContainerName();
}
