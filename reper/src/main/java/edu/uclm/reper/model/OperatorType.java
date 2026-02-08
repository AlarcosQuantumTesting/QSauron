package edu.uclm.reper.model;

import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "operator_types")
public class OperatorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "name")
    private String name;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "operatorType_id")
    private List<Operator> operators;

    public List<Operator> getOperators() {
        return operators;
    }

    public void setOperators(List<Operator> operators) {
        this.operators = operators;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
