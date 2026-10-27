package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blocks")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "block_circuit_id")
    @JsonBackReference
    private BlockCircuit blockCircuit;

    @Column(name = "numberOfLeftColumns")
    private int numberOfLeftColumns;

    @Column(name = "numberOfRightColumns")
    private int numberOfRightColumns;

    @jakarta.persistence.Convert(converter = StringMatrixConverter.class)
    @Column(columnDefinition = "JSON")
    private List<List<String>> leftColumns;

    @jakarta.persistence.Convert(converter = StringMatrixConverter.class)
    @Column(columnDefinition = "JSON")
    private List<List<String>> rightColumns;

    public Block() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BlockCircuit getBlockCircuit() {
        return blockCircuit;
    }

    public void setBlockCircuit(BlockCircuit blockCircuit) {
        this.blockCircuit = blockCircuit;
    }

    public int getNumberOfLeftColumns() {
        return numberOfLeftColumns;
    }

    public void setNumberOfLeftColumns(int numberOfLeftColumns) {
        this.numberOfLeftColumns = numberOfLeftColumns;
    }

    public int getNumberOfRightColumns() {
        return numberOfRightColumns;
    }

    public void setNumberOfRightColumns(int numberOfRightColumns) {
        this.numberOfRightColumns = numberOfRightColumns;
    }

    public List<List<String>> getLeftColumns() {
        return leftColumns;
    }

    public void setLeftColumns(List<List<String>> leftColumns) {
        this.leftColumns = leftColumns;
    }

    public List<List<String>> getRightColumns() {
        return rightColumns;
    }

    public void setRightColumns(List<List<String>> rightColumns) {
        this.rightColumns = rightColumns;
    }

}
