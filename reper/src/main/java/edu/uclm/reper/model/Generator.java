package edu.uclm.reper.model;

import jakarta.persistence.*;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Grenoble.class, name = "GRENOBLE"),
    @JsonSubTypes.Type(value = Matrix.class, name = "MATRIX"),
    @JsonSubTypes.Type(value = GroverAndRudolph.class, name = "GROVER_RUDOLPH"),
    @JsonSubTypes.Type(value = Genetic.class, name = "GENETIC"),
    @JsonSubTypes.Type(value = Grover.class, name = "GROVER"),
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
