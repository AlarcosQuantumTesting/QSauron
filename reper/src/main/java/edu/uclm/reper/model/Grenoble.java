package edu.uclm.reper.model;

import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

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

    @ElementCollection
    @CollectionTable(name = "grenoble_position_values", joinColumns = @JoinColumn(name = "grenoble_id"))
    @MapKeyColumn(name = "position_key")
    @Column(name = "position_value")
    private Map<Integer, Integer> positionValue;


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

    public Map<Integer, Integer> getPositionValue() {
        return positionValue;
    }

    public void setPositionValue(Map<Integer, Integer> positionValue) {
        this.positionValue = positionValue;
    }
}
