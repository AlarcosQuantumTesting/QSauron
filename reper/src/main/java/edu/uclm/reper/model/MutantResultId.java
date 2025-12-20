package edu.uclm.reper.model;

import java.io.Serializable;
import java.util.Objects;

public class MutantResultId implements Serializable {
    private int id;
    private int mutant;

    public MutantResultId() {
    }

    public MutantResultId(int id, int mutant) {
        this.id = id;
        this.mutant = mutant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMutant() {
        return mutant;
    }

    public void setMutant(int mutant) {
        this.mutant = mutant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MutantResultId that = (MutantResultId) o;
        return id == that.id && mutant == that.mutant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mutant);
    }
}
