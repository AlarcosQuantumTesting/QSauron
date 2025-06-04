package edu.uclm.reper.model;

/**
 * User entity representing system users
 */
public class User {
    private String id;

    // Constructors
    public User() {}

    public User(String id) {
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
