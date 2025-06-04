package edu.uclm.reper.model;

/**
 * Circuit entity representing quantum circuits
 * Note: This class was referenced in Project but not defined in the PlantUML diagram
 */
public class Circuit {
    private int id;
    private String name;
    private String description;

    // Constructors
    public Circuit() {}

    public Circuit(String name, String description) {
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
