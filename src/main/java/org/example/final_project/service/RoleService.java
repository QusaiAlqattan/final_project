package org.example.final_project.service;

import org.example.final_project.dto.RoleDTO;
import org.example.final_project.model.Role;
import org.example.final_project.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch role
    //  !   ///////////////////////////////////////////////////////////////
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> convertedRoles = new ArrayList<>();
        for (Role role : roles) {
            convertedRoles.add(toDTO(role));
        }
        return convertedRoles;
    }

    // convert for model to DTO
    private RoleDTO toDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setUniqueId(role.getUniqueId());
        roleDTO.setName(role.getName());
        return roleDTO;
    }
    //  !   ///////////////////////////////////////////////////////////////
}
