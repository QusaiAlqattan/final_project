package org.example.final_project.model;

import jakarta.persistence.*;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    @ManyToOne
    @JoinColumn(name = "writer_id")
    private SystemUser writer;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private File file;

    @Lob
    private String content;

    @Column(nullable = false)
    private int rowNumber;

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public SystemUser getWriter() {
        return writer;
    }

    public void setWriter(SystemUser writer) {
        this.writer = writer;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
}