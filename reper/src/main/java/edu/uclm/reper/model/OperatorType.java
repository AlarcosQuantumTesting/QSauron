package edu.uclm.reper.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "operator_types")
public class OperatorType {

    @Id
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
