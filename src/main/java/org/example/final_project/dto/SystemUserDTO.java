package org.example.final_project.dto;

import java.util.List;

public class SystemUserDTO {

    private Long uniqueId;
    private String username;
    private String password; // Consider removing this in DTOs to avoid sending sensitive data
    private Integer githubID;
    private Long roleId; // ID of the associated role
    private List<Long> fileIds; // List of IDs of files created by this user
    private List<Long> noteIds; // List of IDs of notes written by this user

    // Getters and Setters

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password; // Ensure to handle sensitive information appropriately
    }

    public Integer getGithubID() {
        return githubID;
    }

    public void setGithubID(Integer githubID) {
        this.githubID = githubID;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    public List<Long> getNoteIds() {
        return noteIds;
    }

    public void setNoteIds(List<Long> noteIds) {
        this.noteIds = noteIds;
    }
}