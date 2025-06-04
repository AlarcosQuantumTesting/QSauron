package edu.uclm.reper.model;

/**
 * KillingMatrix entity representing the matrix of mutant results
 */
public class KillingMatrix {
    private int id;
    private MutantResult mutantResults;

    // Constructors
    public KillingMatrix() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MutantResult getMutantResults() {
        return mutantResults;
    }

    public void setMutantResults(MutantResult mutantResults) {
        this.mutantResults = mutantResults;
    }
}
