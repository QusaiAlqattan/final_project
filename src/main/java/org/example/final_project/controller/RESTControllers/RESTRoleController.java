package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.RoleDTO;
import org.example.final_project.model.Role;
import org.example.final_project.repository.RoleRepository;
import org.example.final_project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RESTRoleController {

    @Autowired
    private RoleService roleService;

    // Endpoint to fetch all roles
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
