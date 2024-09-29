package org.example.final_project.repository;

import org.example.final_project.model.Folder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomFolderRepositoryImpl implements CustomFolderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Folder> findAllWithContainerName() {
        List<Folder> folders = entityManager.createQuery("SELECT f FROM Folder f", Folder.class).getResultList();

        // Populate container name for each folder
        for (Folder folder : folders) {
            if (folder.getContainer() != null) {
                folder.setContainerName(folder.getContainer().getName()); // Assuming you add a setContainerName method
            }
        }

        return folders;
    }
}
