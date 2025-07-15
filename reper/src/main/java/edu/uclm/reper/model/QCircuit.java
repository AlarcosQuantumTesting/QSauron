package edu.uclm.reper.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Qcircuits")
public class QCircuit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    
    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    
    public QCircuit() {}

    public QCircuit(int qbits, int[] inputQubits, int[] outputQubits, String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
