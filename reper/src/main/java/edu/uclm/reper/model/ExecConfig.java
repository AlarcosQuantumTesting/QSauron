package edu.uclm.reper.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "exec_configs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execution_date")
    private Date executionDate;
    
    @Column(name = "machine")
    private String machine;
    
    @Column(name = "exec_algorithm")
    private String execAlgorithm;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "killing_matrix_id")
    private KillingMatrix killingMatrix;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutant_cycle_id")
    @JsonBackReference("mutantCycle-execConfig")
    private MutantCycle mutantCycle;

    // Constructors
    public ExecConfig() {}

    public ExecConfig(Date executionDate, String machine, String execAlgorithm) {
        this.executionDate = executionDate;
        this.machine = machine;
        this.execAlgorithm = execAlgorithm;
    }    // Getters and Setters
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
    }    public void setKillingMatrix(KillingMatrix killingMatrix) {
        this.killingMatrix = killingMatrix;
    }

    public MutantCycle getMutantCycle() {
        return mutantCycle;
    }

    public void setMutantCycle(MutantCycle mutantCycle) {
        this.mutantCycle = mutantCycle;
    }
}
