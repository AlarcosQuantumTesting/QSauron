package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;



@Entity
@Table(name = "genetic_generators")
@DiscriminatorValue("GENETIC")
public class Genetic extends Generator {

    @ElementCollection
    @CollectionTable(name = "genetic_hadamards", joinColumns = @JoinColumn(name = "genetic_id"))
    @Column(name = "hadamard")
    private List<Boolean> hadamards;
    
    @Column(name = "min_columns")
    private int minColumns;
    
    @Column(name = "max_columns")
    private int maxColumns;
    
    @Column(name = "init_pop_size")
    private int initPopSize;
    
    @Column(name = "max_pop_size")
    private int maxPopSize;
    
    @Column(name = "desired_error")
    private float desiredError;
    
    @ElementCollection
    @CollectionTable(name = "genetic_gates", joinColumns = @JoinColumn(name = "genetic_id"))
    @Column(name = "gate")
    private List<String> gates;


    public Genetic() {}

    public List<Boolean> getHadamards() {
        return hadamards;
    }

    public void setHadamards(List<Boolean> hadamards) {
        this.hadamards = hadamards;
    }

    public int getMinColumns() {
        return minColumns;
    }

    public void setMinColumns(int minColumns) {
        this.minColumns = minColumns;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    public int getInitPopSize() {
        return initPopSize;
    }

    public void setInitPopSize(int initPopSize) {
        this.initPopSize = initPopSize;
    }

    public int getMaxPopSize() {
        return maxPopSize;
    }

    public void setMaxPopSize(int maxPopSize) {
        this.maxPopSize = maxPopSize;
    }

    public float getDesiredError() {
        return desiredError;
    }

    public void setDesiredError(float desiredError) {
        this.desiredError = desiredError;
    }

    public List<String> getGates() {
        return gates;
    }

    public void setGates(List<String> gates) {
        this.gates = gates;
    }
}
