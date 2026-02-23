package edu.uclm.reper.dto;

import java.util.List;

public class ProjectDetailsDTO {
    private String id;
    private String name;
    private QuCoDetailsDTO quCoDetails;
    private List<QuTeDetailsDTO> quTeDetails;
    private List<QuMuDetailsDTO> quMuDetails;
    private List<ProjectNoteDTO> projectNotes;

    public ProjectDetailsDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuCoDetailsDTO getQuCoDetails() {
        return quCoDetails;
    }

    public void setQuCoDetails(QuCoDetailsDTO quCoDetails) {
        this.quCoDetails = quCoDetails;
    }

    public List<QuTeDetailsDTO> getQuTeDetails() {
        return quTeDetails;
    }

    public void setQuTeDetails(List<QuTeDetailsDTO> quTeDetails) {
        this.quTeDetails = quTeDetails;
    }

    public List<QuMuDetailsDTO> getQuMuDetails() {
        return quMuDetails;
    }

    public void setQuMuDetails(List<QuMuDetailsDTO> quMuDetails) {
        this.quMuDetails = quMuDetails;
    }

    public List<ProjectNoteDTO> getProjectNotes() {
        return projectNotes;
    }

    public void setProjectNotes(List<ProjectNoteDTO> projectNotes) {
        this.projectNotes = projectNotes;
    }
}
