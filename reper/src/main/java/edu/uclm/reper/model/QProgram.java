package edu.uclm.reper.model;

import java.util.List;

import jakarta.persistence.*;

/**
 * QProgram entity representing quantum programs
 */
@Entity
@Table(name = "q_programs")
public class QProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "qiskit_code", columnDefinition = "TEXT")
    private String qiskitCode;
    
    @Column(name = "qubits")
    private int qubits;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "expression_id")
    private Expression expressions;
    
    @Column(name = "shots")
    private int shots;
    
    @Column(name = "input_qubits")
    private int[] inputQubits;
    
    @Column(name = "output_qubits")
    private int[] outputQubits;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuirkCircuit> quirkCircuits;

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "integrated_circuit_id", nullable = true)
    private QuirkCircuit integratedCircuit;

    public QProgram() {}   
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQiskitCode() {
        return qiskitCode;
    }

    public void setQiskitCode(String qiskitCode) {
        this.qiskitCode = qiskitCode;
    }

    public int getQubits() {
        return qubits;
    }

    public void setQubits(int qubits) {
        this.qubits = qubits;
    }

    public Expression getExpressions() {
        return expressions;
    }

    public void setExpressions(Expression expressions) {
        this.expressions = expressions;
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
}
