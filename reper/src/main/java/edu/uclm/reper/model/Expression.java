package edu.uclm.reper.model;

/**
 * Expression entity representing quantum expressions
 */
public class Expression {
    private String name;
    private String expr;
    private String description;
    private String type;

    // Constructors
    public Expression() {}

    public Expression(String name, String expr, String description, String type) {
        this.name = name;
        this.expr = expr;
        this.description = description;
        this.type = type;
    }

    // Getters and Setters
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
