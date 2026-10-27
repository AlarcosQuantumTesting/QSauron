package edu.uclm.reper.dto;

import java.util.List;

public class ProjectSummaryDTO {
    private String id;
    private String name;
    private List<MutantCycleSummaryDTO> mutantCycles;
    private Integer qubits;
    private String inputQubits;
    private String outputQubits;

    public ProjectSummaryDTO() {}

    public ProjectSummaryDTO(String id, String name, List<MutantCycleSummaryDTO> mutantCycles) {
        this.id = id;
        this.name = name;
        this.mutantCycles = mutantCycles;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<MutantCycleSummaryDTO> getMutantCycles() { return mutantCycles; }
    public void setMutantCycles(List<MutantCycleSummaryDTO> mutantCycles) { this.mutantCycles = mutantCycles; }

    public Integer getQubits() { return qubits; }
    public void setQubits(Integer qubits) { this.qubits = qubits; }

    public String getInputQubits() { return inputQubits; }
    public void setInputQubits(String inputQubits) { this.inputQubits = inputQubits; }

    public String getOutputQubits() { return outputQubits; }
    public void setOutputQubits(String outputQubits) { this.outputQubits = outputQubits; }
}
