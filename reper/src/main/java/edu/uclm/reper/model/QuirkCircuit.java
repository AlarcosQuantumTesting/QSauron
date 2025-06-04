package edu.uclm.reper.model;

/**
 * QuirkCircuit entity representing quantum circuit definitions
 */
public class QuirkCircuit {
    private int qbits;
    private int[] inputQubits;
    private int[] outputQubits;
    private String code;

    // Constructors
    public QuirkCircuit() {}

    public QuirkCircuit(int qbits, int[] inputQubits, int[] outputQubits, String code) {
        this.qbits = qbits;
        this.inputQubits = inputQubits;
        this.outputQubits = outputQubits;
        this.code = code;
    }

    // Getters and Setters
    public int getQbits() {
        return qbits;
    }

    public void setQbits(int qbits) {
        this.qbits = qbits;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
