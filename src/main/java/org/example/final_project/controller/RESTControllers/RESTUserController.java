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
@RequestMapping("admin/api/users")
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
    public ResponseEntity<Map<String, String>> getUserIdAndUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String id = String.valueOf(systemUserRepository.findByUsername(username).getUniqueId());

        // Create a response map
        Map<String, String> response = new HashMap<>();
        response.put("userId", id);
        response.put("userName", username);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody SystemUserDTO systemUserDTO) {
        try {
            systemUserService.createUser(systemUserDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestBody SystemUserDTO userDTO) {
        systemUserService.updateUserRole(id, userDTO.getRoleId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        systemUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}