package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

    @javax.persistence.Convert(converter = StringListListConverter.class)
    @Column(name = "startingColumns", columnDefinition = "TEXT")
    private List<List<String>> startingColumns;

    @OneToMany(mappedBy = "blockCircuit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
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
