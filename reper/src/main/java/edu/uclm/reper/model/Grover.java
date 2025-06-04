package edu.uclm.reper.model;

import java.util.List;

/**
 * Grover algorithm generator for quantum circuits
 */
public class Grover extends Generator {
    private List<Object> truePositions;

    // Constructors
    public Grover() {}

    // Getters and Setters
    public List<Object> getTruePositions() {
        return truePositions;
    }

    public void setTruePositions(List<Object> truePositions) {
        this.truePositions = truePositions;
    }
}
