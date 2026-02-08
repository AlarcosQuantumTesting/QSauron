package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * TestCase entity representing individual test cases
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Stochastic.class, name = "STOCHASTIC"),
        @JsonSubTypes.Type(value = Deterministic.class, name = "DETERMINISTIC")
})
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "test_cases")
public class TestCase {
    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    @JsonBackReference("testSuite-testCases")
    private TestSuite testSuite;

    @Column(name = "entry_indexes")
    private int[] entryIndexes;

    @Column(name = "output_indexes")
    private int[] outputIndexes;

    public TestCase() {
    }

    public TestCase(int[] entryIndexes, int[] outputIndexes) {
        this.entryIndexes = entryIndexes;
        this.outputIndexes = outputIndexes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public int[] getEntryIndexes() {
        return entryIndexes;
    }

    public void setEntryIndexes(int[] entryIndexes) {
        this.entryIndexes = entryIndexes;
    }

    public int[] getOutputIndexes() {
        return outputIndexes;
    }

    public void setOutputIndexes(int[] outputIndexes) {
        this.outputIndexes = outputIndexes;
    }
}
