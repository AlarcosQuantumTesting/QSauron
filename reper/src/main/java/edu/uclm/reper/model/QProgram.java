package edu.uclm.reper.model;

/**
 * QProgram entity representing quantum programs
 */
public class QProgram {
    private int id;
    private String name;
    private String qiskitCode;
    private int qubits;
    private Expression expressions;
    private int shots;
    private int[] inputQubits;
    private int[] outputQubits;

    // Constructors
    public QProgram() {}

    // Getters and Setters
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
