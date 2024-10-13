package org.example.final_project.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.REMOVE)
    @JsonManagedReference // Tells Jackson to manage the serialization of subFolders
    private List<Folder> folders;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.REMOVE)
    @JsonManagedReference // Tells Jackson to manage the serialization of subFolders
    private List<File> files;

    private LocalDateTime timestamp;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

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

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}