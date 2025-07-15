package edu.uclm.reper.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "mutants")
public class Mutant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "mutant_index")
    private int index;
    
    @Column(name = "column_pos")
    private int column;
    
    @Column(name = "row_pos")
    private int row;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quirk_circuit_id")
    private QCircuit quirkCircuit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutant_cycle_id")
    private MutantCycle mutantCycle;

    @OneToMany(mappedBy = "mutant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MutantResult> mutantResults;

    // Constructors
    public Mutant() {}

    public Mutant(int index, int column, int row) {
        this.index = index;
        this.column = column;
        this.row = row;
    }
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
    }    public void setRow(int row) {
        this.row = row;
    }

    public QCircuit getQuirkCircuit() {
        return quirkCircuit;
    }

    public void setQuirkCircuit(QCircuit quirkCircuit) {
        this.quirkCircuit = quirkCircuit;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public MutantCycle getMutantCycle() {
        return mutantCycle;
    }

    public void setMutantCycle(MutantCycle mutantCycle) {
        this.mutantCycle = mutantCycle;
    }

    public List<MutantResult> getMutantResults() {
        return mutantResults;
    }

    public void setMutantResults(List<MutantResult> mutantResults) {
        this.mutantResults = mutantResults;
    }


}
