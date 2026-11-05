package edu.uclm.reper.dto;

import java.util.Date;

public class ExperimentListItemDTO {
    private String id;
    private String name;
    private String status;
    private Date createdAt;
    private Date finishedAt;
    private Long durationSeconds;
    private Integer totalCircuits;
    private Integer completedCircuits;
    private boolean hadFailures;
    private boolean resumable;

    public ExperimentListItemDTO(String id, String name, String status, Date createdAt, Date finishedAt, Long durationSeconds, Integer totalCircuits, Integer completedCircuits, Boolean hadFailures) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
        this.durationSeconds = durationSeconds;
        this.totalCircuits = totalCircuits;
        this.completedCircuits = completedCircuits;
        this.hadFailures = Boolean.TRUE.equals(hadFailures);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public Date getFinishedAt() { return finishedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public Integer getTotalCircuits() { return totalCircuits; }
    public Integer getCompletedCircuits() { return completedCircuits; }
    public boolean isHadFailures() { return hadFailures; }
    public boolean isResumable() { return resumable; }
    public void setResumable(boolean resumable) { this.resumable = resumable; }
}
