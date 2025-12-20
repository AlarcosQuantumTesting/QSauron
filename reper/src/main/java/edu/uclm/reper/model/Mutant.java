package edu.uclm.reper.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mutants")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mutant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "mutant_index")
    @JsonProperty("mutantIndex")
    private int index;

    @Column(name = "column_pos")
    @JsonProperty("mutatedColumn")
    private int column;

    @Column(name = "row_pos")
    @JsonProperty("mutatedRow")
    private int row;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quirk_circuit_id")
    @JsonProperty("circuit")
    private QProgram quirkCircuit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutant_cycle_id")
    @JsonBackReference("mutantCycle-mutants")
    private MutantCycle mutantCycle;

    @OneToMany(mappedBy = "mutant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty("mutantResults")
    @JsonManagedReference("mutant-results")
    private List<MutantResult> mutantResults;

    // Constructors
    public Mutant() {
    }

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
    }

    public void setRow(int row) {
        this.row = row;
    }

    public QProgram getQuirkCircuit() {
        return quirkCircuit;
    }

    public void setQuirkCircuit(QProgram quirkCircuit) {
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
