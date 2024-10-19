package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.SystemUserDTO;
import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping
public class RESTRegisterController {

    private final SystemUserService systemUserService;

    @Autowired
    public RESTRegisterController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody SystemUserDTO systemUserDTO) {
        try {
            systemUserService.createUser(systemUserDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating user: " + e.getMessage());
        }
    }
}