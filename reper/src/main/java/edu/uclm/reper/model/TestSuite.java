package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_suites")
public class TestSuite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonBackReference("project-testSuites")
    private Project project;

    @OneToMany(mappedBy = "testSuite", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("testSuite-testCases")
    private List<TestCase> testCases;

    @Column(name = "error_range")
    private int error_range;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "test_suite_entry_values",
        joinColumns = @JoinColumn(name = "test_suite_id")
    )
    @OrderColumn(name = "pos")
    @Column(name = "value", length = 8)
    @JsonProperty("entryValues")
    private List<String> entryValues;

    public TestSuite() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

    public int getError_range() { return error_range; }
    public void setError_range(int error_range) { this.error_range = error_range; }

    public List<String> getEntryValues() { return entryValues; }
    public void setEntryValues(List<String> entryValues) { this.entryValues = entryValues; }
}
