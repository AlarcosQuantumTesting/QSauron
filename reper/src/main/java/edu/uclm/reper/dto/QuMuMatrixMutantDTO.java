package edu.uclm.reper.dto;

import java.util.List;

public class QuMuMatrixMutantDTO {
    private int id;
    private int mutantIndex;
    private int mutatedColumn;
    private int mutatedRow;
    private OperatorSummaryDTO operator;
    private String oracleName;
    private String mutationDetail;
    private List<QuMuResultDTO> mutantResults;

    public QuMuMatrixMutantDTO() {
    }

    public QuMuMatrixMutantDTO(int id, int mutantIndex, int mutatedColumn, int mutatedRow, OperatorSummaryDTO operator, String oracleName, String mutationDetail, List<QuMuResultDTO> mutantResults) {
        this.id = id;
        this.mutantIndex = mutantIndex;
        this.mutatedColumn = mutatedColumn;
        this.mutatedRow = mutatedRow;
        this.operator = operator;
        this.oracleName = oracleName;
        this.mutationDetail = mutationDetail;
        this.mutantResults = mutantResults;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMutantIndex() {
        return mutantIndex;
    }

    public void setMutantIndex(int mutantIndex) {
        this.mutantIndex = mutantIndex;
    }

    public int getMutatedColumn() {
        return mutatedColumn;
    }

    public void setMutatedColumn(int mutatedColumn) {
        this.mutatedColumn = mutatedColumn;
    }

    public int getMutatedRow() {
        return mutatedRow;
    }

    public void setMutatedRow(int mutatedRow) {
        this.mutatedRow = mutatedRow;
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

    public String getMutationDetail() {
        return mutationDetail;
    }

    public void setMutationDetail(String mutationDetail) {
        this.mutationDetail = mutationDetail;
    }

    public List<QuMuResultDTO> getMutantResults() {
        return mutantResults;
    }

    public void setMutantResults(List<QuMuResultDTO> mutantResults) {
        this.mutantResults = mutantResults;
    }
}
