package org.example.final_project.repository;

import org.example.final_project.model.File;
import org.example.final_project.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByBranch_UniqueId(Long branchId);
}
