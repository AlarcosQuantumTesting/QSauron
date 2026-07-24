package edu.uclm.reper.dto;

public class MutantSummaryDTO {
    private int mutantIndex;
    private OperatorSummaryDTO operator;
    private String oracleName;

    public MutantSummaryDTO() {
    }

    public MutantSummaryDTO(int mutantIndex, OperatorSummaryDTO operator) {
        this.mutantIndex = mutantIndex;
        this.operator = operator;
    }

    public MutantSummaryDTO(int mutantIndex, OperatorSummaryDTO operator, String oracleName) {
        this.mutantIndex = mutantIndex;
        this.operator = operator;
        this.oracleName = oracleName;
    }

    public int getMutantIndex() {
        return mutantIndex;
    }

    public void setMutantIndex(int mutantIndex) {
        this.mutantIndex = mutantIndex;
    }

    public OperatorSummaryDTO getOperator() {
        return operator;
    }

    public void setOperator(OperatorSummaryDTO operator) {
        this.operator = operator;
    }

    public String getOracleName() {
        return oracleName;
    }

    public void setOracleName(String oracleName) {
        this.oracleName = oracleName;
    }
}
