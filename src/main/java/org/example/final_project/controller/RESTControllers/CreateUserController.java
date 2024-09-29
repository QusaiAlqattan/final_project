package org.example.final_project.controller.RESTControllers;

import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.service.SystemUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // or @Controller if returning views
@RequestMapping("/admin") // Base URL for the controller
public class CreateUserController {

    private final SystemUserRepository systemUserRepository;

    @Autowired
    public CreateUserController(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    // Handle the form submission for creating a new user
    @PostMapping() // Endpoint to create a user
    public ResponseEntity<String> createUser(@ModelAttribute SystemUser user) {
        systemUserRepository.save(user); // Save the user to the database
        return new ResponseEntity<>("User created successfully", HttpStatus.CREATED); // Respond with success
    }
}