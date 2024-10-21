package org.example.final_project.service;

import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.model.Role;
import org.example.final_project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class GitHubUserService {

    private final SystemUserRepository systemUserRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public GitHubUserService(SystemUserRepository systemUserRepository, RoleRepository roleRepository) {
        this.systemUserRepository = systemUserRepository;
        this.roleRepository = roleRepository;
    }

    public SystemUser processOAuthPostLogin(OAuth2User oauth2User) {
        String username = oauth2User.getAttribute("login");
        Integer githubID = oauth2User.getAttribute("id");

        SystemUser user = systemUserRepository.findByUsername(username);
        if (user == null) {
            user = new SystemUser();
            user.setUsername(username);
            user.setGithubID(githubID);
        }

        if (roleRepository.findByName("USER") != null){
            user.setRole(roleRepository.findByName("USER"));
        }else{
            Role User = new Role();
            User.setName("USER");
            roleRepository.save(User);

            Role Admin = new Role();
            Admin.setName("ADMIN");
            roleRepository.save(Admin);

            user.setRole(User);
        }
        systemUserRepository.save(user);

        return user;
    }
}
