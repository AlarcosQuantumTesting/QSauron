package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Project entity representing quantum mutation testing projects
 */
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<MutantCycle> mutantCycles;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "test_suite_id")
    private TestSuite testSuite;    
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "q_program_id")
    private QProgram qProgram;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_projects",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"projects"})
    private List<User> users;

    // Constructors
    public Project() {}

    public Project(String name) {
        this.name = name;
    }    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public QProgram getqProgram() {
        return qProgram;
    }    public void setqProgram(QProgram qProgram) {
        this.qProgram = qProgram;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
