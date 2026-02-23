package edu.uclm.reper.dto;

import java.util.Date;

public class ProjectNoteDTO {
    private String id;
    private String title;
    private String text;
    private String type;
    private Date timestamp;

    public ProjectNoteDTO() {
    }

    public ProjectNoteDTO(String id, String title, String text, String type, Date timestamp) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
