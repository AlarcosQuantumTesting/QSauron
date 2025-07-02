package edu.uclm.reper.model;

import jakarta.persistence.*;

/**
 * QuirkCircuit entity representing quantum circuit definitions
 */
@Entity
@Table(name = "quirk_circuits")
public class QuirkCircuit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "qbits")
    private int qbits;
    
    @Column(name = "input_qubits")
    private int[] inputQubits;
    
    @Column(name = "output_qubits")
    private int[] outputQubits;
    
    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    
    public QuirkCircuit() {}

    public QuirkCircuit(int qbits, int[] inputQubits, int[] outputQubits, String code) {
        this.qbits = qbits;
        this.inputQubits = inputQubits;
        this.outputQubits = outputQubits;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
