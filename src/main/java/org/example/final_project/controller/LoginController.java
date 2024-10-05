package org.example.final_project.controller;

import org.example.final_project.repository.RoleRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping
public class LoginController {

    private final RoleRepository roleRepository;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public LoginController(RoleRepository roleRepository, SystemUserRepository systemUserRepository) {
        this.roleRepository = roleRepository;
        this.systemUserRepository = systemUserRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the login view
    }
}
