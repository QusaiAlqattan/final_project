package org.example.final_project.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EditorController {

    @GetMapping("/file/{fileId}")
    public String serveEditorPage(Model model, @PathVariable String fileId) {
        return "editor.html";
    }
}
