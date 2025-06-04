package edu.uclm.reper.model;

/**
 * Mutant entity representing circuit mutations
 */
public class Mutant {
    private int index;
    private int column;
    private int row;

    // Constructors
    public Mutant() {}

    public Mutant(int index, int column, int row) {
        this.index = index;
        this.column = column;
        this.row = row;
    }

    // Getters and Setters
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
