package edu.uclm.reper.model;

import java.io.Serializable;
import java.util.Objects;

public class MutantCycleId implements Serializable {
    private String project;
    private int id;

    public MutantCycleId() {
    }

    public MutantCycleId(String project, int id) {
        this.project = project;
        this.id = id;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MutantCycleId that = (MutantCycleId) o;
        return id == that.id && Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, id);
    }
}
