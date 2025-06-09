package edu.uclm.reper.model;

import jakarta.persistence.*;

/**
 * Grenoble generator for quantum circuits
 */
@Entity
@Table(name = "grenoble_generators")
@DiscriminatorValue("GRENOBLE")
public class Grenoble extends Generator {
    @Column(name = "interesting_rows")
    private int interestingRows;
    
    @Column(name = "physical_angle")
    private float physicalAngle;
    
    @Column(name = "parallel")
    private boolean parallel;
    
    @Column(name = "splitted")
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
