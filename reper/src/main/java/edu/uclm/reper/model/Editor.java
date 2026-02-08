package edu.uclm.reper.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "editor_generators")
@DiscriminatorValue("EDITOR")
public class Editor extends Generator {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "editor_id")
    @org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<EditorGates> gates;

    @Column(name = "columns")
    private int columns;

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public Editor() {
    }

    public List<EditorGates> getGates() {
        return gates;
    }

    public void setGates(List<EditorGates> gates) {
        this.gates = gates;
    }
}
