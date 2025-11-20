package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "editor_generators")
@DiscriminatorValue("EDITOR")
public class Editor  extends Generator{
    
    @Column(name ="gateId")
    @JsonProperty("id")
    private String gateId;

    @Column(name ="column")
    private int column;
    
    @Column(name ="name")
    private String name;

    @Column(name ="editor_qubits")
    private int[] qubits;

    @Column(name ="parentQubit")
    private int parentQubit;

    @Column(name ="transactionId")
    private String transactionId;

    public Editor() {
    }


    public String getGateId() {
        return gateId;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getQubits() {
        return qubits;
    }

    public void setQubits(int[] qubits) {
        this.qubits = qubits;
    }

    public int getParentQubit() {
        return parentQubit;
    }

    public void setParentQubit(int parentQubit) {
        this.parentQubit = parentQubit;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    

}
