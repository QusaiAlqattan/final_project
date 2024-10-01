package org.example.final_project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    private String name;
    private String content;
    private String version;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    @JsonBackReference // Prevents infinite recursion when serializing
    private Folder container;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonBackReference // Prevents infinite recursion when serializing
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private SystemUser creator;

    @OneToMany(mappedBy = "file")
    private List<Note> notes;

//    @Transient  // This field won't be persisted in the database
//    private String containerName;
//
//    @Transient  // This field won't be persisted in the database
//    private String branchName;
//
//    @Transient
//    private Long containerId;  // Not stored in DB, but captured from request
//
//    @Transient
//    private Long branchId;     // Not stored in DB, but captured from request

    // Getters and Setters...

//    public Long getContainerId() {
//        return containerId;
//    }
//
//    public void setContainerId(Long containerId) {
//        this.containerId = containerId;
//    }
//
//    public Long getBranchId() {
//        return branchId;
//    }
//
//    public void setBranchId(Long branchId) {
//        this.branchId = branchId;
//    }
//
//    public String getContainerName() {
//        return containerName;
//    }
//
//    public void setContainerName(String containerName) {
//        this.containerName = containerName;
//    }
//
//    public String getBranchName() {
//        return branchName;
//    }
//
//    public void setBranchName(String branchName) {
//        this.branchName = branchName;
//    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Folder getContainer() {
        return container; // Updated getter
    }

    public void setContainer(Folder container) { // Updated setter
        this.container = container;
    }

    public SystemUser getCreator() {
        return creator;
    }

    public void setCreator(SystemUser creator) {
        this.creator = creator;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String contents) {
        this.content = contents;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}