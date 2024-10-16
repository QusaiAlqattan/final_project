package org.example.final_project.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BranchDTO {

    private Long uniqueId;
    private String name;
    private List<Long> folderIds;
    private List<Long> fileIds;
    private LocalDateTime timestamp;
    private Long parentBranchId;

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

    public List<Long> getFolderIds() {
        return folderIds;
    }

    public void setFolderIds(List<Long> folderIds) {
        this.folderIds = folderIds;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getParentBranchId() {
        return parentBranchId;
    }

    public void setParentBranchId(Long parentBranchId) {
        this.parentBranchId = parentBranchId;
    }
}
