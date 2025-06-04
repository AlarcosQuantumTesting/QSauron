package edu.uclm.reper.model;

import java.util.Date;

/**
 * ExecConfig entity representing execution configuration
 */
public class ExecConfig {
    private int id;
    private Date executionDate;
    private String machine;
    private String execAlgorithm;
    private KillingMatrix killingMatrix;

    // Constructors
    public ExecConfig() {}

    public ExecConfig(Date executionDate, String machine, String execAlgorithm) {
        this.executionDate = executionDate;
        this.machine = machine;
        this.execAlgorithm = execAlgorithm;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public KillingMatrix getKillingMatrix() {
        return killingMatrix;
    }

    public void setKillingMatrix(KillingMatrix killingMatrix) {
        this.killingMatrix = killingMatrix;
    }
}
