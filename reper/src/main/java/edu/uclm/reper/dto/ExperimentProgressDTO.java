package edu.uclm.reper.dto;

import java.util.Date;

public class ExperimentProgressDTO {
    private String id;
    private String name;
    private String status;
    private int totalCircuits;
    private int completedCircuits;
    private double percentage;
    private Long durationSeconds;
    private Date startedAt;

    public ExperimentProgressDTO() {}

    public ExperimentProgressDTO(String id, String name, String status, int totalCircuits, int completedCircuits, Date startedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.totalCircuits = totalCircuits;
        this.completedCircuits = completedCircuits;
        this.startedAt = startedAt;
        this.percentage = totalCircuits > 0 ? ((double) completedCircuits / totalCircuits) * 100.0 : 0.0;
        if (startedAt != null) {
            this.durationSeconds = (System.currentTimeMillis() - startedAt.getTime()) / 1000;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalCircuits() { return totalCircuits; }
    public void setTotalCircuits(int totalCircuits) { this.totalCircuits = totalCircuits; }

    public int getCompletedCircuits() { return completedCircuits; }
    public void setCompletedCircuits(int completedCircuits) { this.completedCircuits = completedCircuits; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }
}
