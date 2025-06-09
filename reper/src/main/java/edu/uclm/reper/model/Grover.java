package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Grover algorithm generator for quantum circuits
 */
@Entity
@Table(name = "grover_generators")
@DiscriminatorValue("GROVER")
public class Grover extends Generator {
    
    @ElementCollection
    @CollectionTable(name = "grover_true_positions", joinColumns = @JoinColumn(name = "grover_id"))
    @Column(name = "true_position")
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
