package edu.uclm.reper.dto;

import java.util.List;

public class MergeExperimentsDTO {
    private String email;
    private List<String> experimentIds;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getExperimentIds() { return experimentIds; }
    public void setExperimentIds(List<String> experimentIds) { this.experimentIds = experimentIds; }
}
