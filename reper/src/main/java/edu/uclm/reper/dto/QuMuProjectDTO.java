package edu.uclm.reper.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uclm.reper.model.TestSuite;

public class QuMuProjectDTO {
    private String id;
    private String name;

    @JsonProperty("qProgram")
    private QuMuProgramDTO qProgram;

    private List<QuMuCycleSummaryDTO> mutantCycles;
    private List<ProjectNoteDTO> projectNotes;
    private List<TestSuite> testSuites;

    public QuMuProjectDTO() {
    }

    public QuMuProjectDTO(String id, String name, QuMuProgramDTO qProgram, List<QuMuCycleSummaryDTO> mutantCycles, List<ProjectNoteDTO> projectNotes, List<TestSuite> testSuites) {
        this.id = id;
        this.name = name;
        this.qProgram = qProgram;
        this.mutantCycles = mutantCycles;
        this.projectNotes = projectNotes;
        this.testSuites = testSuites;
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

    public QuMuProgramDTO getQProgram() {
        return qProgram;
    }

    public void setQProgram(QuMuProgramDTO qProgram) {
        this.qProgram = qProgram;
    }

    public List<QuMuCycleSummaryDTO> getMutantCycles() {
        return mutantCycles;
    }

    public void setMutantCycles(List<QuMuCycleSummaryDTO> mutantCycles) {
        this.mutantCycles = mutantCycles;
    }

    public List<ProjectNoteDTO> getProjectNotes() {
        return projectNotes;
    }

    public void setProjectNotes(List<ProjectNoteDTO> projectNotes) {
        this.projectNotes = projectNotes;
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public void setTestSuites(List<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }
}
