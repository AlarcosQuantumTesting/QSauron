package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USUARIO;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "users", "mutantCycles", "qProgram", "testSuite" })
    private List<Project> projects;

    // Constructors
    public User() {
        this.role = Role.USUARIO;
    }

    public User(String id) {
        this.id = id;
        this.role = Role.USUARIO;
    }

    public User(String id, Role role) {
        this.id = id;
        this.role = role != null ? role : Role.USUARIO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role != null ? role : Role.USUARIO;
    }

    public void setRole(Role role) {
        this.role = role != null ? role : Role.USUARIO;
    }

    public boolean isAdmin() {
        return Role.ADMINISTRADOR.equals(this.role);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
