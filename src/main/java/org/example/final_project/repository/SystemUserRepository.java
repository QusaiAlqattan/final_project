package org.example.final_project.repository;

import org.example.final_project.model.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {
    SystemUser findByUsername(String username);

    Optional<SystemUser> findByGithubID(Integer githubId);

//    default String getUserRole(String username) {
//        SystemUser user = findByUsername(username);
//        return (user != null && user.getRole() != null) ? user.getRole().getName() : null;
//    }
}