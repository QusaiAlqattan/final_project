package org.example.final_project.service;


import org.example.final_project.dto.NoteDTO;
import org.example.final_project.model.File;
import org.example.final_project.model.Note;
import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.NoteRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;


@Service
public class NoteService {

    private final SystemUserRepository systemUserRepository;
    private final FileRepository fileRepository;
    private final NoteRepository noteRepository;


    public NoteService(SystemUserRepository systemUserRepository, FileRepository fileRepository, NoteRepository noteRepository) {
        this.systemUserRepository = systemUserRepository;
        this.fileRepository = fileRepository;
        this.noteRepository = noteRepository;
    }

    public String createNote(Long fileId, NoteDTO noteDTO) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        System.out.println(username);
        System.out.println(username.getClass());
        SystemUser user = systemUserRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        System.out.println("user" + user);

        // Get the file associated with the note
        Optional<File> fileOptional = fileRepository.findById(fileId);
        if (!fileOptional.isPresent()) {
            return "File not found";
        }

        // Create and save the note
        Note note = new Note();
        note.setWriter(user);
        note.setFile(fileOptional.get());
        note.setContent(noteDTO.getContent());
        note.setRowNumber(noteDTO.getRowNumber());

        noteRepository.save(note);

        return "Note added successfully";
    }

    public List<NoteDTO> getNotesByFileId(Long fileId) {
        List<Note> notes = noteRepository.findByFileUniqueId(fileId);
        List<NoteDTO> noteDTOList = new ArrayList<>();
        for (Note note : notes) {
            NoteDTO noteDTO = toDTO(note);
            noteDTOList.add(noteDTO);
        }
        return noteDTOList;
    }

    private NoteDTO toDTO(Note note) {
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setContent(note.getContent());
        noteDTO.setRowNumber(note.getRowNumber());
        noteDTO.setWriterName(note.getWriter().getUsername());
        return noteDTO;
    }

}
