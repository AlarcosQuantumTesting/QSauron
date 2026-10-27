package edu.uclm.reper.dto;

import java.util.List;

public class MutantCycleSummaryDTO {
    private int id;
    private String name;
    private String projectId;
    private String projectName;
    private int killedCount;
    private int zombieCount;
    private int aliveCount;
    private Double mutationScore;
    private List<MutantSummaryDTO> mutants;

    public MutantCycleSummaryDTO() {
    }

    public MutantCycleSummaryDTO(int id, String name, List<MutantSummaryDTO> mutants) {
        this.id = id;
        this.name = name;
        this.mutants = mutants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<MutantSummaryDTO> getMutants() {
        return mutants;
    }

    public void setMutants(List<MutantSummaryDTO> mutants) {
        this.mutants = mutants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getKilledCount() {
        return killedCount;
    }

    public void setKilledCount(int killedCount) {
        this.killedCount = killedCount;
    }

    public int getZombieCount() {
        return zombieCount;
    }

    public void setZombieCount(int zombieCount) {
        this.zombieCount = zombieCount;
    }

    public int getAliveCount() {
        return aliveCount;
    }

    public void setAliveCount(int aliveCount) {
        this.aliveCount = aliveCount;
    }

    public Double getMutationScore() {
        return mutationScore;
    }

    public void setMutationScore(Double mutationScore) {
        this.mutationScore = mutationScore;
    }
}
