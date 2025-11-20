package edu.uclm.reper.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * QProgram entity representing quantum programs
 */
@Entity
@Table(name = "q_programs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QProgram {
    @Id
    @Column(name = "id")
    private String id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "q_program_id")
    private List<QCode> qCodes;

    @Column(name = "qubits")
    private int qubits;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "q_program_id")
    private List<Expression> expressions;

    @Column(name = "shots")
    private int shots;

    @Column(name = "input_qubits")
    private int[] inputQubits;

    @Column(name = "output_qubits")
    private int[] outputQubits;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "circuit_id")
    @JsonProperty("qCircuit")
    private QCircuit quirkCircuit;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "generator_id")
    private Generator generator;

    public QProgram() {
    }

    public List<QCode> getQCodes() {
        return qCodes;
    }

    public void setQCodes(List<QCode> qCodes) {
        this.qCodes = qCodes;
    }

    public QCircuit getQuirkCircuit() {
        return quirkCircuit;
    }

    public void setQuirkCircuit(QCircuit quirkCircuit) {
        this.quirkCircuit = quirkCircuit;
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

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
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

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    @JsonSetter("inputQubits")
    public void setInputQubits(String inputQubits) {
        this.inputQubits = Arrays.stream(inputQubits.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    @JsonSetter("outputQubits")
    public void setOutputQubits(String str) {

        this.outputQubits = Arrays.stream(str.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

}
