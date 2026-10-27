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

    public ExperimentListItemDTO(String id, String name, String status, Date createdAt, Date finishedAt, Long durationSeconds, Integer totalCircuits, Integer completedCircuits) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
        this.durationSeconds = durationSeconds;
        this.totalCircuits = totalCircuits;
        this.completedCircuits = completedCircuits;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public Date getFinishedAt() { return finishedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public Integer getTotalCircuits() { return totalCircuits; }
    public Integer getCompletedCircuits() { return completedCircuits; }
}
