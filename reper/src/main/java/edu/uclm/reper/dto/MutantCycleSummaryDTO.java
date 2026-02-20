package edu.uclm.reper.dto;

import java.util.List;

public class MutantCycleSummaryDTO {
    private int id;
    private List<MutantSummaryDTO> mutants;

    public MutantCycleSummaryDTO() {}

    public MutantCycleSummaryDTO(int id, List<MutantSummaryDTO> mutants) {
        this.id = id;
        this.mutants = mutants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<MutantSummaryDTO> getMutants() {
        return mutants;
    }

    public void setMutants(List<MutantSummaryDTO> mutants) {
        this.mutants = mutants;
    }
}
