package edu.uclm.reper.dto;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuMuCycleMatrixDTO {
    private int id;
    private String name;
    private Date date;

    @JsonProperty("execConfiguration")
    private QuMuExecConfigDTO execConfiguration;

    private List<QuMuMatrixMutantDTO> mutants;

    public QuMuCycleMatrixDTO() {
    }

    public QuMuCycleMatrixDTO(int id, String name, Date date, QuMuExecConfigDTO execConfiguration, List<QuMuMatrixMutantDTO> mutants) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.execConfiguration = execConfiguration;
        this.mutants = mutants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public QuMuExecConfigDTO getExecConfiguration() {
        return execConfiguration;
    }

    public void setExecConfiguration(QuMuExecConfigDTO execConfiguration) {
        this.execConfiguration = execConfiguration;
    }

    public List<QuMuMatrixMutantDTO> getMutants() {
        return mutants;
    }

    public void setMutants(List<QuMuMatrixMutantDTO> mutants) {
        this.mutants = mutants;
    }
}
