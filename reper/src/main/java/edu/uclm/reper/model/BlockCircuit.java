package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "block_circuits")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockCircuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numberOfStartColumns")
    private int numberOfStartColumns;

    @Column(name = "numberOfBlocks")
    private int numberOfBlocks;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<List<String>> startingColumns;

    @OneToMany(mappedBy = "blockCircuit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Block> blocks;

    public BlockCircuit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumberOfStartColumns() {
        return numberOfStartColumns;
    }

    public void setNumberOfStartColumns(int numberOfStartColumns) {
        this.numberOfStartColumns = numberOfStartColumns;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public List<List<String>> getStartingColumns() {
        return startingColumns;
    }

    public void setStartingColumns(List<List<String>> startingColumns) {
        this.startingColumns = startingColumns;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

}
