package edu.uclm.reper.model;

import jakarta.persistence.*;

@Entity
@Table(name = "expressions")
public class Expression {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "expr")
    private String expr;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "type")
    private String type;

    
    public Expression() {}

    public Expression(String name, String expr, String description, String type) {
        this.name = name;
        this.expr = expr;
        this.description = description;
        this.type = type;
    }    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
