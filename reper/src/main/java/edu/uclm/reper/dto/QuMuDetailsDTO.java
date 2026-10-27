package edu.uclm.reper.dto;

public class QuMuDetailsDTO {
    private int mutantsCount;

    public QuMuDetailsDTO() {
    }

    public QuMuDetailsDTO(int mutantsCount) {
        this.mutantsCount = mutantsCount;
    }

    public int getMutantsCount() {
        return mutantsCount;
    }

    public void setMutantsCount(int mutantsCount) {
        this.mutantsCount = mutantsCount;
    }
}
