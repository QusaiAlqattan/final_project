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

@RestController
@RequestMapping("/api/users") // Base URL for the controller
public class RESTUserController {

    private final SystemUserService systemUserService;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public RESTUserController(SystemUserService systemUserService, SystemUserRepository systemUserRepository) {
        this.systemUserService = systemUserService;
        this.systemUserRepository = systemUserRepository;
    }

    @GetMapping()
    public List<SystemUserDTO> getAllUsers() {
        return systemUserService.getAllUsers();
    }

    @GetMapping("/id")
    public String getUserId() {
        System.out.println("77777777777777777777777777777777");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String id = String.valueOf(systemUserRepository.findByUsername(username).getUniqueId());
        return id;
    }


    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody SystemUserDTO systemUserDTO) {
        try {
            systemUserService.createUser(systemUserDTO); // Implement this method to handle user creation logic
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestBody SystemUserDTO userDTO) {
        systemUserService.updateUserRole(id, userDTO.getRoleId()); // Update user role
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        systemUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}