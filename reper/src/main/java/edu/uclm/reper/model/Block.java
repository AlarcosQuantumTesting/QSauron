package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

    @javax.persistence.Convert(converter = StringListListConverter.class)
    @Column(name = "leftColumns", columnDefinition = "TEXT")
    private List<List<String>> leftColumns;

    @javax.persistence.Convert(converter = StringListListConverter.class)
    @Column(name = "rightColumns", columnDefinition = "TEXT")
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
