package org.example.final_project.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "version", "branch_id"})
})
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
    @JsonBackReference
    private Folder container;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonBackReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private SystemUser creator;

    @OneToMany(mappedBy = "file", cascade = CascadeType.REMOVE)
    private List<Note> notes;

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Folder getContainer() {
        return container;
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        File file = (File) o;
//
//        return Objects.equals(name, file.name) &&
//                Objects.equals(version, file.version);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name, version);
//    }
}