package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Project entity representing quantum mutation testing projects
 */
@Entity
@Table(name = "projects")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("project-mutantCycles")
    private List<MutantCycle> mutantCycles;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("project-testSuites")
    private List<TestSuite> testSuites;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "q_program_id")
    private QProgram qProgram;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_projects", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties({ "projects" })
    private List<User> users;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "editor_id")
    @JsonProperty("projectNotes")
    private List<ProjectNote> notes;

    // Constructors
    public Project() {
    }

    public Project(String name) {
        this.name = name;
    } // Getters and Setters

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

    public List<MutantCycle> getMutantCycles() {
        return mutantCycles;
    }

    public void setMutantCycles(List<MutantCycle> mutantCycles) {
        this.mutantCycles = mutantCycles;
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public void setTestSuites(List<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public QProgram getqProgram() {
        return qProgram;
    }

    public void setqProgram(QProgram qProgram) {
        this.qProgram = qProgram;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<ProjectNote> getNotes() {
        return notes;
    }

    public void setNotes(List<ProjectNote> notes) {
        this.notes = notes;
    }
}
