package edu.uclm.reper.model;

import java.util.Date;

/**
 * MutantCycle entity representing a cycle of mutant execution
 */
public class MutantCycle {
    private int id;
    private Date date;
    private Mutant mutants;
    private ExecConfig execConfigs;

    // Constructors
    public MutantCycle() {}

    public MutantCycle(Date date) {
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Mutant getMutants() {
        return mutants;
    }

    public void setMutants(Mutant mutants) {
        this.mutants = mutants;
    }

    public ExecConfig getExecConfigs() {
        return execConfigs;
    }

    public void setExecConfigs(ExecConfig execConfigs) {
        this.execConfigs = execConfigs;
    }
}
