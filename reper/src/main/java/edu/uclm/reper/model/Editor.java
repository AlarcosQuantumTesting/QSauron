package edu.uclm.reper.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "editor_generators")
@DiscriminatorValue("EDITOR")
public class Editor  extends Generator{
   
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "editor_id")
    private List<EditorGates> gates;

    public Editor() {
    }

    public List<EditorGates> getGates() {
        return gates;
    }

    public void setGates(List<EditorGates> gates) {
        this.gates = gates;
    }
    

    

}
