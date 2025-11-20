package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "editor_gates")
public class EditorGates {
    
    @Id
    @Column(name = "gate_id")
    @JsonProperty("id")
    private String gateId;
    
    @Column(name = "editor_id", insertable = false, updatable = false)
    private Long editorId;
    
    @Column(name = "column_number")
    private Integer column;
    
    @Column(name = "gate_name")
    private String name;
    
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> qubits;
    
    @Column(name = "parent_qubit")
    private Integer parentQubit;
    
    @Column(name = "transaction_id")
    private String transactionId;

    public EditorGates() {
    }

    public String getGateId() {
        return gateId;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getQubits() {
        return qubits;
    }

    public void setQubits(List<Integer> qubits) {
        this.qubits = qubits;
    }

    public Integer getParentQubit() {
        return parentQubit;
    }

    public void setParentQubit(Integer parentQubit) {
        this.parentQubit = parentQubit;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}