package edu.uclm.reper.dto;

public class CycleStatsDTO {
    private int id;
    private String name;
    private String projectId;
    private String projectName;
    private long totalMutants;
    private long killedCount;
    private long zombieCount;
    private long aliveCount;
    private long notExecutedCount;
    private Double mutationScore;
    private Boolean generationCompleted;
    private String inputQubits;
    private String outputQubits;

    public CycleStatsDTO() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public long getTotalMutants() { return totalMutants; }
    public void setTotalMutants(long totalMutants) { this.totalMutants = totalMutants; }

    public long getKilledCount() { return killedCount; }
    public void setKilledCount(long killedCount) { this.killedCount = killedCount; }

    public long getZombieCount() { return zombieCount; }
    public void setZombieCount(long zombieCount) { this.zombieCount = zombieCount; }

    public long getAliveCount() { return aliveCount; }
    public void setAliveCount(long aliveCount) { this.aliveCount = aliveCount; }

    public long getNotExecutedCount() { return notExecutedCount; }
    public void setNotExecutedCount(long notExecutedCount) { this.notExecutedCount = notExecutedCount; }

    public Double getMutationScore() { return mutationScore; }
    public void setMutationScore(Double mutationScore) { this.mutationScore = mutationScore; }

    public Boolean getGenerationCompleted() { return generationCompleted; }
    public void setGenerationCompleted(Boolean generationCompleted) { this.generationCompleted = generationCompleted; }

    public String getInputQubits() { return inputQubits; }
    public void setInputQubits(String inputQubits) { this.inputQubits = inputQubits; }

    public String getOutputQubits() { return outputQubits; }
    public void setOutputQubits(String outputQubits) { this.outputQubits = outputQubits; }
}
