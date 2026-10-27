package edu.uclm.reper.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "experiments")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Experiment {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private String status; // RUNNING, COMPLETED, FAILED, CANCELLED

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at")
    private Date finishedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "total_circuits")
    private Integer totalCircuits = 0;

    @Column(name = "completed_circuits")
    private Integer completedCircuits = 0;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "projects", "experiments" })
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "exec_config_id")
    private ExecConfig execConfig;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("experiment-mutantCycles")
    private List<MutantCycle> mutantCycles = new ArrayList<>();

    public Experiment() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.status = "RUNNING";
    }

    public Experiment(String name, User user) {
        this();
        this.name = name;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getTotalCircuits() {
        return totalCircuits;
    }

    public void setTotalCircuits(Integer totalCircuits) {
        this.totalCircuits = totalCircuits;
    }

    public Integer getCompletedCircuits() {
        return completedCircuits;
    }

    public void setCompletedCircuits(Integer completedCircuits) {
        this.completedCircuits = completedCircuits;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ExecConfig getExecConfig() {
        return execConfig;
    }

    public void setExecConfig(ExecConfig execConfig) {
        this.execConfig = execConfig;
    }

    public List<MutantCycle> getMutantCycles() {
        return mutantCycles;
    }

    public void setMutantCycles(List<MutantCycle> mutantCycles) {
        this.mutantCycles = mutantCycles;
    }
}
