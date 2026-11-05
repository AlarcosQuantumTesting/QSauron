package edu.uclm.reper.dto;

import java.util.List;

public class ResumeExperimentDTO {

    public static class CycleQubits {
        private String projectId;
        private int cycleId;
        private String inputQubits;
        private String outputQubits;

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public int getCycleId() { return cycleId; }
        public void setCycleId(int cycleId) { this.cycleId = cycleId; }
        public String getInputQubits() { return inputQubits; }
        public void setInputQubits(String inputQubits) { this.inputQubits = inputQubits; }
        public String getOutputQubits() { return outputQubits; }
        public void setOutputQubits(String outputQubits) { this.outputQubits = outputQubits; }
    }

    private String email;
    private String experimentId;
    private List<CycleQubits> cycles;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String experimentId) { this.experimentId = experimentId; }
    public List<CycleQubits> getCycles() { return cycles; }
    public void setCycles(List<CycleQubits> cycles) { this.cycles = cycles; }
}
