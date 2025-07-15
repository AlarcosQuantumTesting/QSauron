package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    private String id;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private List<Project> projects;

    // Constructors
    public User() {}

    public User(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }    public void setId(String id) {
        this.id = id;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
