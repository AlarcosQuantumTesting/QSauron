package edu.uclm.reper.model;

/**
 * Grenoble generator for quantum circuits
 */
public class Grenoble extends Generator {
    private int interestingRows;
    private float physicalAngle;
    private boolean parallel;
    private boolean splitted;

    // Constructors
    public Grenoble() {}

    // Getters and Setters
    public int getInterestingRows() {
        return interestingRows;
    }

    public void setInterestingRows(int interestingRows) {
        this.interestingRows = interestingRows;
    }

    public float getPhysicalAngle() {
        return physicalAngle;
    }

    public void setPhysicalAngle(float physicalAngle) {
        this.physicalAngle = physicalAngle;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public boolean isSplitted() {
        return splitted;
    }

    public void setSplitted(boolean splitted) {
        this.splitted = splitted;
    }
}
