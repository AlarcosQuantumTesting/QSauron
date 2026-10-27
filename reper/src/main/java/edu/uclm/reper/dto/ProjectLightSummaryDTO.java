package edu.uclm.reper.dto;

public class ProjectLightSummaryDTO {

    private String id;
    private String name;
    private boolean hasGeneratedCode;
    private boolean hasMutantCycles;
    private boolean hasTestCases;
    private String generatorType;

    public ProjectLightSummaryDTO() {
    }

    public ProjectLightSummaryDTO(String id, String name, Number hasGeneratedCode, Number hasMutantCycles,
            Number hasTestCases, String generatorType) {
        this.id = id;
        this.name = name;
        this.hasGeneratedCode = hasGeneratedCode != null && hasGeneratedCode.intValue() > 0;
        this.hasMutantCycles = hasMutantCycles != null && hasMutantCycles.intValue() > 0;
        this.hasTestCases = hasTestCases != null && hasTestCases.intValue() > 0;
        this.generatorType = generatorType;
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

    public String getGeneratorType() {
        return generatorType;
    }

    public void setGeneratorType(String generatorType) {
        this.generatorType = generatorType;
    }
}
