package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * TestCase entity representing individual test cases
 */
@Entity
@Table(name = "test_cases")
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "test_index")
    private int index;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    @JsonBackReference("testSuite-testCases")
    private TestSuite testSuite;

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
    }    public void setIndex(int index) {
        this.index = index;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

}
