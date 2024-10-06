package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EditorController {

    @GetMapping("/file/{fileId}")
    public String serveEditorPage() {
        // This will serve the "editor.html" file from the static resources
        return "editor.html";
    }
}
