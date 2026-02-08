package edu.uclm.reper.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * MutantCycle entity representing a cycle of mutant execution
 */
@Entity
@Table(name = "mutant_cycles")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MutantCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date")
    private Date date;

    @OneToMany(mappedBy = "mutantCycle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("mutantCycle-mutants")
    private List<Mutant> mutants;

    @OneToOne(mappedBy = "mutantCycle", cascade = CascadeType.ALL)
    @JsonManagedReference("mutantCycle-execConfig")
    @JsonProperty("execConfiguration")
    private ExecConfig execConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-mutantCycles")
    private Project project;

    // Constructors
    public MutantCycle() {
    }

    public MutantCycle(Date date) {
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Mutant> getMutants() {
        return mutants;
    }

    public void setMutants(List<Mutant> mutants) {
        this.mutants = mutants;
    }

    public ExecConfig getExecConfig() {
        return execConfig;
    }

    public void setExecConfig(ExecConfig execConfig) {
        this.execConfig = execConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
