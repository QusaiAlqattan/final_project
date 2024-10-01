package org.example.final_project.dto;

public class FolderDTO {

    private Long uniqueId;

    private String name;

    private String branchName; // Optional: to represent the branch id

    private String containerName; // Optional: to represent the parent folder id

    private Long branchId; // Optional: to represent the branch id

    private Long containerId; // Optional: to represent the parent folder id

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @Override
    public String toString() {
        return "FolderDTO [uniqueId=" + uniqueId + ", name=" + name + ", branchName=" + branchName + ", containerName=" + containerName + "]";
    }
}
