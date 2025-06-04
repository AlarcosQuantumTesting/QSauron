package edu.uclm.reper.model;

/**
 * TestCase entity representing individual test cases
 */
public class TestCase {
    private Long id;
    private int index;

    // Constructors
    public TestCase() {}

    public TestCase(int index) {
        this.index = index;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
