package edu.uclm.reper.model;

/**
 * MutantResult entity representing the result of a mutant execution
 */
public class MutantResult {
    private Long id;
    private Result result;

    // Constructors
    public MutantResult() {}

    public MutantResult(Result result) {
        this.result = result;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
