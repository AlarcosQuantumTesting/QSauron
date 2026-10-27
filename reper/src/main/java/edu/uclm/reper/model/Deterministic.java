package edu.uclm.reper.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "deterministic_cases")
@DiscriminatorValue("DETERMINISTIC")
public class Deterministic extends TestCase {

    @Column(name = "entry_values")
    private int[] entryValues;

    @Column(name = "expected_values")
    private int[] expectedValues;

    public Deterministic() {
    }

    public Deterministic(int[] entryValues, int[] expectedValues) {
        this.entryValues = entryValues;
        this.expectedValues = expectedValues;
    }

    // Getters and Setters
    public int[] getEntryValues() {
        return entryValues;
    }

    public void setEntryValues(int[] entryValues) {
        this.entryValues = entryValues;
    }

    public int[] getExpectedValues() {
        return expectedValues;
    }

    public void setExpectedValues(int[] expectedValues) {
        this.expectedValues = expectedValues;
    }
}
