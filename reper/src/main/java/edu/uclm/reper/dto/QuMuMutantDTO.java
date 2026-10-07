package edu.uclm.reper.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuMuMutantDTO extends QuMuMatrixMutantDTO {

    @JsonProperty("circuit")
    private QuMuProgramDTO circuit;

    public QuMuMutantDTO() {
        super();
    }

    public QuMuMutantDTO(int id, int mutantIndex, int mutatedColumn, int mutatedRow, OperatorSummaryDTO operator, String oracleName, String mutationDetail, List<QuMuResultDTO> mutantResults, QuMuProgramDTO circuit) {
        super(id, mutantIndex, mutatedColumn, mutatedRow, operator, oracleName, mutationDetail, mutantResults);
        this.circuit = circuit;
    }

    public QuMuProgramDTO getCircuit() {
        return circuit;
    }

    public void setCircuit(QuMuProgramDTO circuit) {
        this.circuit = circuit;
    }
}
