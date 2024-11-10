package org.example.final_project.controller;

import org.example.final_project.model.Role;
import org.example.final_project.model.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.final_project.repository.RoleRepository;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping()
    public String admin_home(Model model) {
        return "admin"; // Return the home view
    }
}
