package edu.uclm.reper.dto;

import java.util.List;

public class MutantCycleSummaryDTO {
    private int id;
    private String name;
    private List<MutantSummaryDTO> mutants;

    public MutantCycleSummaryDTO() {
    }

    public MutantCycleSummaryDTO(int id, String name, List<MutantSummaryDTO> mutants) {
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
