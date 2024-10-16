package org.example.final_project.repository;

import org.example.final_project.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByBranch_UniqueId(Long branchId);
    Optional<File> findByName(String name);
}
