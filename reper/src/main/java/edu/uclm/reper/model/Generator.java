package edu.uclm.reper.model;

import jakarta.persistence.*;


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
