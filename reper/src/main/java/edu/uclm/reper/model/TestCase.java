package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    @JsonBackReference("testSuite-testCases")
    private TestSuite testSuite;

    @Column(name = "entry_indexes")
    private int[] entryIndexes;

    @Column(name = "output_indexes")
    private int[] outputIndexes;

    public TestCase() {}

    public TestCase(int[] entryIndexes, int[] outputIndexes) {
        this.entryIndexes = entryIndexes;
        this.outputIndexes = outputIndexes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TestSuite getTestSuite() { return testSuite; }
    public void setTestSuite(TestSuite testSuite) { this.testSuite = testSuite; }

    public int[] getEntryIndexes() { return entryIndexes; }
    public void setEntryIndexes(int[] entryIndexes) { this.entryIndexes = entryIndexes; }

    public int[] getOutputIndexes() { return outputIndexes; }
    public void setOutputIndexes(int[] outputIndexes) { this.outputIndexes = outputIndexes; }
}
