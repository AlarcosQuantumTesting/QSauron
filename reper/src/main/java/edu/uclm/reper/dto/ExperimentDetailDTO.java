package edu.uclm.reper.dto;

import java.util.Date;
import java.util.List;

public class ExperimentDetailDTO {
    private String id;
    private String name;
    private String status;
    private Date createdAt;
    private Date finishedAt;
    private Long durationSeconds;
    private Integer totalCircuits;
    private Integer completedCircuits;
    private String machine;
    private String execAlgorithm;
    private String errorMessage;
    private List<CycleStatsDTO> cycles;
    private String errorHistory;
    private Integer resumeCount;
    private String mergedFrom;
    private boolean resumable;

    public ExperimentDetailDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Date finishedAt) { this.finishedAt = finishedAt; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getTotalCircuits() { return totalCircuits; }
    public void setTotalCircuits(Integer totalCircuits) { this.totalCircuits = totalCircuits; }

    public Integer getCompletedCircuits() { return completedCircuits; }
    public void setCompletedCircuits(Integer completedCircuits) { this.completedCircuits = completedCircuits; }

    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }

    public String getExecAlgorithm() { return execAlgorithm; }
    public void setExecAlgorithm(String execAlgorithm) { this.execAlgorithm = execAlgorithm; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<CycleStatsDTO> getCycles() { return cycles; }
    public void setCycles(List<CycleStatsDTO> cycles) { this.cycles = cycles; }

    public String getErrorHistory() { return errorHistory; }
    public void setErrorHistory(String errorHistory) { this.errorHistory = errorHistory; }
    public Integer getResumeCount() { return resumeCount; }
    public void setResumeCount(Integer resumeCount) { this.resumeCount = resumeCount; }
    public String getMergedFrom() { return mergedFrom; }
    public void setMergedFrom(String mergedFrom) { this.mergedFrom = mergedFrom; }
    public boolean isResumable() { return resumable; }
    public void setResumable(boolean resumable) { this.resumable = resumable; }
}
