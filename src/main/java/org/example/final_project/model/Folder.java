package org.example.final_project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "container_id")
    @JsonBackReference // Prevents infinite recursion when serializing
    private Folder container;

    @OneToMany(mappedBy = "container")
    @JsonManagedReference // Tells Jackson to manage the serialization of subFolders
    private List<Folder> subFolders;

    @OneToMany(mappedBy = "container")
    @JsonManagedReference // Tells Jackson to manage the serialization of subFolders
    private List<File> files;

    @Transient  // This field won't be persisted in the database
    private String containerName;

    // Existing getters and setters...

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Folder getContainer() {
        return container;
    }

    public void setContainer(Folder container) {
        this.container = container;
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

    public List<Folder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(List<Folder> subFolders) {
        this.subFolders = subFolders;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}