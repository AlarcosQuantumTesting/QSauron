package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.Map;

/**
 * Grover and Rudolph algorithm generator for quantum circuits
 */
@Entity
@Table(name = "grover_rudolph_generators")
@DiscriminatorValue("GROVER_RUDOLPH")
public class GroverAndRudolph extends Generator {
    
    @ElementCollection
    @CollectionTable(name = "grover_rudolph_position_values", joinColumns = @JoinColumn(name = "grover_rudolph_id"))
    @MapKeyColumn(name = "position_key")
    @Column(name = "position_value")
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
