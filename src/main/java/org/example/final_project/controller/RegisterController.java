package org.example.final_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping
public class RegisterController {

    @GetMapping("/register")
    public String register() {
        return "register"; // Return the home view
    }
}
