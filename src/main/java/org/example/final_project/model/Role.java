package org.example.final_project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    private String name;

    @OneToMany(mappedBy = "role")
    private List<SystemUser> systemUsers;

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SystemUser> getUsers() {
        return systemUsers;
    }

    public void setUsers(List<SystemUser> systemUsers) {
        this.systemUsers = systemUsers;
    }
}