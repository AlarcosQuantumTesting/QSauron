package edu.uclm.reper.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "exec_configs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecConfig {
    @Id
    @Column(name = "id")
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execution_date")
    private Date executionDate;

    @Column(name = "machine")
    private String machine;

    @Column(name = "exec_algorithm")
    private String execAlgorithm;

    @Column(name = "tolerated_error")
    private Double toleratedError;

    @Column(name = "zombie_error")
    private Double zombieError;

    @Column(name = "shots")
    private Integer shots;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "killing_matrix_id")
    private KillingMatrix killingMatrix;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "mutant_cycle_id", referencedColumnName = "id"),
            @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    })
    @JsonBackReference("mutantCycle-execConfig")
    private MutantCycle mutantCycle;

    // Constructors
    public ExecConfig() {
    }

    public ExecConfig(Date executionDate, String machine, String execAlgorithm) {
        this.executionDate = executionDate;
        this.machine = machine;
        this.execAlgorithm = execAlgorithm;
    } // Getters and Setters

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

    public KillingMatrix getKillingMatrix() {
        return killingMatrix;
    }

    public void setKillingMatrix(KillingMatrix killingMatrix) {
        this.killingMatrix = killingMatrix;
    }

    public MutantCycle getMutantCycle() {
        return mutantCycle;
    }

    public void setMutantCycle(MutantCycle mutantCycle) {
        this.mutantCycle = mutantCycle;
    }
}
