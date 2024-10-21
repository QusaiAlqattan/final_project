package org.example.final_project.service;

import org.example.final_project.dto.SystemUserDTO;
import org.example.final_project.model.Role;
import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.RoleRepository;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.ArrayList;
import java.util.List;

@Service
public class SystemUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;  // Inject the password encoder

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleService roleService;

    //  !   ///////////////////////////////////////////////////////////////
    //  !   fetch users
    //  !   ///////////////////////////////////////////////////////////////
    public List<SystemUserDTO> getAllUsers() {
        List<SystemUser> users = systemUserRepository.findAll();
        List<SystemUserDTO> convertedUsers = new ArrayList<>();
        for (SystemUser systemUser : users) {
            convertedUsers.add(toDTO(systemUser));
        }
        return convertedUsers;
    }

    // convert for model to DTO
    private SystemUserDTO toDTO(SystemUser systemUser) {
        SystemUserDTO systemUserDTO = new SystemUserDTO();
        systemUserDTO.setUniqueId(systemUser.getUniqueId());
        systemUserDTO.setUsername(systemUser.getUsername());
        systemUserDTO.setRoleId(systemUser.getRole().getUniqueId());
        systemUserDTO.setRole(systemUser.getRole().getName());
        return systemUserDTO;
    }
    //  !   ///////////////////////////////////////////////////////////////


    //  !   ///////////////////////////////////////////////////////////////
    //  !   create users
    //  !   ///////////////////////////////////////////////////////////////
    public void createUser(@RequestBody SystemUserDTO systemUserDTO) {
        SystemUser user = new SystemUser();
        user.setUsername(systemUserDTO.getUsername());

        // Encrypt the password before saving it
        String encodedPassword = passwordEncoder.encode(systemUserDTO.getPassword());
        user.setPassword(encodedPassword); // Store the encoded password

        if (roleRepository.findByName("USER") != null){
            user.setRole(roleRepository.findByName("USER"));
        }else{
            Role User = new Role();
            User.setName("USER");
            roleRepository.save(User);

            Role Admin = new Role();
            Admin.setName("ADMIN");
            roleRepository.save(Admin);

            user.setRole(Admin);
        }

        systemUserRepository.save(user);
    }
    //  !   ///////////////////////////////////////////////////////////////


    //  !   ///////////////////////////////////////////////////////////////
    //  !   edit users
    //  !   ///////////////////////////////////////////////////////////////
    public void updateUserRole(Long userId, Long newRoleId) {
        SystemUser user = systemUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(newRole); // Update the user's role
        systemUserRepository.save(user); // Save the changes
    }
    //  !   ///////////////////////////////////////////////////////////////


    //  !   ///////////////////////////////////////////////////////////////
    //  !   delete users
    //  !   ///////////////////////////////////////////////////////////////
    public void deleteUser(Long id) {
        systemUserRepository.deleteById(id); // Delete the user
    }
    //  !   ///////////////////////////////////////////////////////////////
}
