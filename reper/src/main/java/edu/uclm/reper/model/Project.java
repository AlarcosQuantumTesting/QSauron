package edu.uclm.reper.model;

/**
 * Project entity representing quantum mutation testing projects
 */
public class Project {
    private int id;
    private String name;
    private Circuit circuit;
    private MutantCycle mutantCycles;
    private TestSuite testSuite;

    // Constructors
    public Project() {}

    public Project(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
    }

    public MutantCycle getMutantCycles() {
        return mutantCycles;
    }

    public void setMutantCycles(MutantCycle mutantCycles) {
        this.mutantCycles = mutantCycles;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }
}
