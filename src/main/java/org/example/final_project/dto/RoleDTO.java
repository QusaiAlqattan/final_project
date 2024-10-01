package org.example.final_project.dto;

import java.util.List;

public class RoleDTO {

    private Long uniqueId;
    private String name;
    private List<Long> userIds; // List of IDs of associated SystemUsers

    // Getters and Setters

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}