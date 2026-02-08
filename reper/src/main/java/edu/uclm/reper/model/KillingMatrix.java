package edu.uclm.reper.model;

import javax.persistence.*;
import java.util.List;

/**
 * KillingMatrix entity representing the matrix of mutant results
 */
@Entity
@Table(name = "killing_matrices")
public class KillingMatrix {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "killing_matrix_id")
    private List<MutantResult> mutantResults;

    public KillingMatrix() {} 
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<MutantResult> getMutantResults() {
        return mutantResults;
    }

    public void setMutantResults(List<MutantResult> mutantResults) {
        this.mutantResults = mutantResults;
    }
}
