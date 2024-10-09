package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/branches")
public class BranchesController {

    @GetMapping()
    public String user_home() {
        return "branches"; // Return the home view
    }
}
