package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Grenoble.class, name = "GRENOBLE"),
        @JsonSubTypes.Type(value = Matrix.class, name = "MATRIX"),
        @JsonSubTypes.Type(value = Genetic.class, name = "GENETIC"),
        @JsonSubTypes.Type(value = Grover.class, name = "GROVER"),
        @JsonSubTypes.Type(value = Blocks.class, name = "BLOCKS"),
        @JsonSubTypes.Type(value = Editor.class, name = "EDITOR"),
})

@Entity
@Table(name = "generators")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "generator_type")
public abstract class Generator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
