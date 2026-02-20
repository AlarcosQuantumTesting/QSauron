package edu.uclm.reper.dto;

public class MutantSummaryDTO {
    private int mutantIndex;
    private OperatorSummaryDTO operator;
    
    public MutantSummaryDTO() {}

    public MutantSummaryDTO(int mutantIndex, OperatorSummaryDTO operator) {
        this.mutantIndex = mutantIndex;
        this.operator = operator;
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
}
