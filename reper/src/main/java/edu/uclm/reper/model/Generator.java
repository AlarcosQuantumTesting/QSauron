package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

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
