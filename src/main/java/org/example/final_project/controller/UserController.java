package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping()
    public String user_home() {
        return "user"; // Return the home view
    }
}
