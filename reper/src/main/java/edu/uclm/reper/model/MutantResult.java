package edu.uclm.reper.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * MutantResult entity representing the result of a mutant execution
 */
@Entity
@Table(name = "mutant_results")
@IdClass(MutantResultId.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MutantResult {

    @Id
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private Result result;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutant_id")
    @JsonBackReference("mutant-results")
    private Mutant mutant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    @JsonBackReference("testCase-mutantResults")
    private TestCase testCase;

    // Constructors
    public MutantResult() {
    }

    public MutantResult(Result result) {
        this.result = result;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Mutant getMutant() {
        return mutant;
    }

    public void setMutant(Mutant mutant) {
        this.mutant = mutant;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }
}
