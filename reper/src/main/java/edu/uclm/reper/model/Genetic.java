package edu.uclm.reper.model;

import java.util.List;

/**
 * Genetic algorithm generator for quantum circuits
 */
public class Genetic extends Generator {
    private List<Object> hadamards;
    private int minColumns;
    private int maxColumns;
    private int initPopSize;
    private int maxPopSize;
    private float desiredError;
    private List<Object> gates;

    // Constructors
    public Genetic() {}

    // Getters and Setters
    public List<Object> getHadamards() {
        return hadamards;
    }

    public void setHadamards(List<Object> hadamards) {
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

    public List<Object> getGates() {
        return gates;
    }

    public void setGates(List<Object> gates) {
        this.gates = gates;
    }
}
