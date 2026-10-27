package edu.uclm.reper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuMuProgramDTO {
    private String id;
    private int qubits;
    private int shots;
    private int[] inputQubits;
    private int[] outputQubits;

    @JsonProperty("qCircuit")
    private QuMuCircuitDTO qCircuit;

    public QuMuProgramDTO() {
    }

    public QuMuProgramDTO(String id, int qubits, int shots, int[] inputQubits, int[] outputQubits, QuMuCircuitDTO qCircuit) {
        this.id = id;
        this.qubits = qubits;
        this.shots = shots;
        this.inputQubits = inputQubits;
        this.outputQubits = outputQubits;
        this.qCircuit = qCircuit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQubits() {
        return qubits;
    }

    public void setQubits(int qubits) {
        this.qubits = qubits;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public int[] getInputQubits() {
        return inputQubits;
    }

    public void setInputQubits(int[] inputQubits) {
        this.inputQubits = inputQubits;
    }

    public int[] getOutputQubits() {
        return outputQubits;
    }

    public void setOutputQubits(int[] outputQubits) {
        this.outputQubits = outputQubits;
    }

    public QuMuCircuitDTO getQCircuit() {
        return qCircuit;
    }

    public void setQCircuit(QuMuCircuitDTO qCircuit) {
        this.qCircuit = qCircuit;
    }
}
