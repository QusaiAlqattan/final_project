package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EditorController {

    @GetMapping("/file/{fileId}")
    public String serveEditorPage() {
        return "editor.html";
    }
}
