package edu.uclm.reper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuMuMutantCircuitDTO {
    private int mutantIndex;

    @JsonProperty("circuit")
    private QuMuProgramDTO circuit;

    public QuMuMutantCircuitDTO() {
    }

    public QuMuMutantCircuitDTO(int mutantIndex, QuMuProgramDTO circuit) {
        this.mutantIndex = mutantIndex;
        this.circuit = circuit;
    }

    public int getMutantIndex() {
        return mutantIndex;
    }

    public void setMutantIndex(int mutantIndex) {
        this.mutantIndex = mutantIndex;
    }

    public QuMuProgramDTO getCircuit() {
        return circuit;
    }

    public void setCircuit(QuMuProgramDTO circuit) {
        this.circuit = circuit;
    }
}
