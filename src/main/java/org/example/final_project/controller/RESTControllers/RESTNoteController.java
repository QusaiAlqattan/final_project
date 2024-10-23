package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.NoteDTO;
import org.example.final_project.repository.FileRepository;
import org.example.final_project.repository.NoteRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RESTNoteController {

    private final NoteService noteService;

    public RESTNoteController( NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/notes/{fileId}")
    public ResponseEntity<List<NoteDTO>> getNotesByFile(@PathVariable Long fileId) {
        List<NoteDTO> noteDTOS = noteService.getNotesByFileId(fileId);
        return ResponseEntity.ok(noteDTOS);
    }

    @PostMapping("/note/add/{fileId}")
    public ResponseEntity<String> addNote(@PathVariable Long fileId, @RequestBody NoteDTO noteDTO) {
        return ResponseEntity.ok(noteService.createNote(fileId, noteDTO));
    }
}
