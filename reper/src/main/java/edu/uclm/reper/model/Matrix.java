package edu.uclm.reper.model;

import java.util.Map;

/**
 * Matrix generator for quantum circuits
 */
public class Matrix extends Generator {
    private int interestingRows;
    private Map<Object, Object> positionValue;

    // Constructors
    public Matrix() {}

    // Getters and Setters
    public int getInterestingRows() {
        return interestingRows;
    }

    public void setInterestingRows(int interestingRows) {
        this.interestingRows = interestingRows;
    }

    public Map<Object, Object> getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(Map<Object, Object> positionValue) {
        this.positionValue = positionValue;
    }
}
