package edu.uclm.reper.dto;

import java.util.Date;

public class QuMuExecConfigDTO {
    private String id;
    private Date executionDate;
    private String machine;
    private String execAlgorithm;
    private Double toleratedError;
    private Double zombieError;
    private Integer shots;

    public QuMuExecConfigDTO() {
    }

    public QuMuExecConfigDTO(String id, Date executionDate, String machine, String execAlgorithm, Double toleratedError, Double zombieError, Integer shots) {
        this.id = id;
        this.executionDate = executionDate;
        this.machine = machine;
        this.execAlgorithm = execAlgorithm;
        this.toleratedError = toleratedError;
        this.zombieError = zombieError;
        this.shots = shots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getExecAlgorithm() {
        return execAlgorithm;
    }

    public void setExecAlgorithm(String execAlgorithm) {
        this.execAlgorithm = execAlgorithm;
    }

    public Double getToleratedError() {
        return toleratedError;
    }

    public void setToleratedError(Double toleratedError) {
        this.toleratedError = toleratedError;
    }

    public Double getZombieError() {
        return zombieError;
    }

    public void setZombieError(Double zombieError) {
        this.zombieError = zombieError;
    }

    public Integer getShots() {
        return shots;
    }

    public void setShots(Integer shots) {
        this.shots = shots;
    }
}
