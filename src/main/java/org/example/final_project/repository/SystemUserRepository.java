package org.example.final_project.repository;

import org.example.final_project.model.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {
    SystemUser findByUsername(String username);

    default String getUserRole(String username) {
        SystemUser user = findByUsername(username);
        return (user != null && user.getRole() != null) ? user.getRole().getName() : null;
    }
}