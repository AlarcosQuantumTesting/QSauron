package edu.uclm.reper.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
    
    @OneToMany(mappedBy = "mutantCycle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("mutantCycle-mutants")
    private List<Mutant> mutants;
    
    @OneToOne(mappedBy = "mutantCycle", cascade = CascadeType.ALL)
    @JsonManagedReference("mutantCycle-execConfig")
    private ExecConfig execConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-mutantCycles")
    private Project project;

    // Constructors
    public MutantCycle() {}

    public MutantCycle(Date date) {
        this.date = date;
    }    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }    public void setDate(Date date) {
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
