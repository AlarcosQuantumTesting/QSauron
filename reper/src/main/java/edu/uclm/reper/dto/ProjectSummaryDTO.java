package edu.uclm.reper.dto;

import java.util.List;

public class ProjectSummaryDTO {
    private String id;
    private String name;
    private List<MutantCycleSummaryDTO> mutantCycles;

    public ProjectSummaryDTO() {}

    public ProjectSummaryDTO(String id, String name, List<MutantCycleSummaryDTO> mutantCycles) {
        this.id = id;
        this.name = name;
        this.mutantCycles = mutantCycles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MutantCycleSummaryDTO> getMutantCycles() {
        return mutantCycles;
    }

    public void setMutantCycles(List<MutantCycleSummaryDTO> mutantCycles) {
        this.mutantCycles = mutantCycles;
    }
}
