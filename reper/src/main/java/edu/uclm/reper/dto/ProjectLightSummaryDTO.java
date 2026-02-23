package edu.uclm.reper.dto;

public class ProjectLightSummaryDTO {

    private String id;
    private String name;
    private boolean hasGeneratedCode;
    private boolean hasMutantCycles;
    private boolean hasTestCases;

    public ProjectLightSummaryDTO() {
    }

    public ProjectLightSummaryDTO(String id, String name, boolean hasGeneratedCode, boolean hasMutantCycles,
            boolean hasTestCases) {
        this.id = id;
        this.name = name;
        this.hasGeneratedCode = hasGeneratedCode;
        this.hasMutantCycles = hasMutantCycles;
        this.hasTestCases = hasTestCases;
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

    public boolean isHasGeneratedCode() {
        return hasGeneratedCode;
    }

    public void setHasGeneratedCode(boolean hasGeneratedCode) {
        this.hasGeneratedCode = hasGeneratedCode;
    }

    public boolean isHasMutantCycles() {
        return hasMutantCycles;
    }

    public void setHasMutantCycles(boolean hasMutantCycles) {
        this.hasMutantCycles = hasMutantCycles;
    }

    public boolean isHasTestCases() {
        return hasTestCases;
    }

    public void setHasTestCases(boolean hasTestCases) {
        this.hasTestCases = hasTestCases;
    }
}
