package edu.uclm.reper.model;

import java.util.Map;

/**
 * Grover and Rudolph algorithm generator for quantum circuits
 */
public class GroverAndRudolph extends Generator {
    private Map<Object, Object> positionValue;

    // Constructors
    public GroverAndRudolph() {}

    // Getters and Setters
    public Map<Object, Object> getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(Map<Object, Object> positionValue) {
        this.positionValue = positionValue;
    }
}
