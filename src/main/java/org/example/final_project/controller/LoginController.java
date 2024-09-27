package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the login view
    }

    @GetMapping("/admin")
    public String admin_home() {
        return "admin_home"; // Return the home view
    }

    @GetMapping("/user")
    public String editor_home() {
        return "user_home"; // Return the home view
    }

    @GetMapping("/register")
    public String register() {
        return "register"; // Return the home view
    }
}
