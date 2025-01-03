package org.example.final_project.controller.RESTControllers;

import org.example.final_project.dto.RoleDTO;
import org.example.final_project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api/roles")
public class RESTRoleController {

    private final RoleService roleService;

    @Autowired
    public RESTRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Endpoint to fetch all roles
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
