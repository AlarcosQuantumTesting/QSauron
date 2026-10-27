package edu.uclm.reper.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuMuCycleSummaryDTO {
    private int id;
    private String name;
    private Date date;

    @JsonProperty("execConfiguration")
    private QuMuExecConfigDTO execConfiguration;

    private long mutantsCount;

    public QuMuCycleSummaryDTO() {
    }

    public QuMuCycleSummaryDTO(int id, String name, Date date, QuMuExecConfigDTO execConfiguration, long mutantsCount) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.execConfiguration = execConfiguration;
        this.mutantsCount = mutantsCount;
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

    public long getMutantsCount() {
        return mutantsCount;
    }

    public void setMutantsCount(long mutantsCount) {
        this.mutantsCount = mutantsCount;
    }
}
