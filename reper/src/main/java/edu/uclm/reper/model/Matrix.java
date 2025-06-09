package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.Map;

/**
 * Matrix generator for quantum circuits
 */
@Entity
@Table(name = "matrix_generators")
@DiscriminatorValue("MATRIX")
public class Matrix extends Generator {
    @Column(name = "interesting_rows")
    private int interestingRows;
    
    @ElementCollection
    @CollectionTable(name = "matrix_position_values", joinColumns = @JoinColumn(name = "matrix_id"))
    @MapKeyColumn(name = "position_key")
    @Column(name = "position_value")
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
