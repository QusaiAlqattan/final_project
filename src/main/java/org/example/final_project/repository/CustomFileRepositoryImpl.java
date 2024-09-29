package org.example.final_project.repository;

import org.example.final_project.model.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomFileRepositoryImpl implements CustomFileRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<File> findAllWithContainerName() {
        // Fetch all File entities
        List<File> files = entityManager.createQuery("SELECT f FROM File f", File.class).getResultList();

        // Set the container (folder) name for each file
        for (File file : files) {
            if (file.getContainer() != null) {
                file.setContainerName(file.getContainer().getName());
            }
        }

        return files;
    }
}
