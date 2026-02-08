package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "editor_gates")
public class EditorGates {

    @Id
    @Column(name = "gate_id")
    @JsonProperty("id")
    private String gateId;

    @Column(name = "editor_id", insertable = false, updatable = false)
    private int editorId;

    @Column(name = "column_number")
    private Integer column;

    @Column(name = "gate_name")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "editor_gates_qubits", joinColumns = @JoinColumn(name = "editor_gates_gate_id"))
    @Column(name = "qubit")
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
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

    public int getEditorId() {
        return editorId;
    }

    public void setEditorId(int editorId) {
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
