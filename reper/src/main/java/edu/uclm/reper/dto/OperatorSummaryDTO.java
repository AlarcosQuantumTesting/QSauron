package edu.uclm.reper.dto;

public class OperatorSummaryDTO {
    private String name;

    public OperatorSummaryDTO() {}

    public OperatorSummaryDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
