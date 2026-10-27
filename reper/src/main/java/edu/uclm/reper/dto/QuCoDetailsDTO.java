package edu.uclm.reper.dto;

public class QuCoDetailsDTO {
    private String generatorType;
    private int qubits;

    public QuCoDetailsDTO() {
    }

    public QuCoDetailsDTO(String generatorType, int qubits) {
        this.generatorType = generatorType;
        this.qubits = qubits;
    }

    public String getGeneratorType() {
        return generatorType;
    }

    public void setGeneratorType(String generatorType) {
        this.generatorType = generatorType;
    }

    public int getQubits() {
        return qubits;
    }

    public void setQubits(int qubits) {
        this.qubits = qubits;
    }
}
