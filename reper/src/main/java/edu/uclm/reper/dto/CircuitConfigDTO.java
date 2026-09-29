package edu.uclm.reper.dto;

public class CircuitConfigDTO {
    private String projectId;
    private String inputQubits;
    private String outputQubits;

    public CircuitConfigDTO() {}

    public CircuitConfigDTO(String projectId, String inputQubits, String outputQubits) {
        this.projectId = projectId;
        this.inputQubits = inputQubits;
        this.outputQubits = outputQubits;
    }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getInputQubits() { return inputQubits; }
    public void setInputQubits(String inputQubits) { this.inputQubits = inputQubits; }

    public String getOutputQubits() { return outputQubits; }
    public void setOutputQubits(String outputQubits) { this.outputQubits = outputQubits; }
}
