package edu.uclm.reper.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_notes")
public class ProjectNote {
    
    @Id
    @Column(name ="id")
    private String id;

    @Column(name ="text")
    private String text;

    @Column(name ="timestamp")
    private Date timestamp;

    public ProjectNote() {
    }

    public ProjectNote(String id, String text, Date timestamp) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



}
