package org.example.final_project.repository;

import org.example.final_project.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByFileUniqueId(Long fileUniqueId);
}
