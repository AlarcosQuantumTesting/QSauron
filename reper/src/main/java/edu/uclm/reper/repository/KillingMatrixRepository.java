package edu.uclm.reper.repository;

import edu.uclm.reper.model.KillingMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for KillingMatrix entity operations
 */
@Repository
public interface KillingMatrixRepository extends JpaRepository<KillingMatrix, Long> {
    
    /**
     * Find killing matrices with mutant results
     */
    @Query("SELECT km FROM KillingMatrix km WHERE SIZE(km.mutantResults) > 0")
    List<KillingMatrix> findMatricesWithResults();
    
    /**
     * Count mutant results in a killing matrix
     */
    @Query("SELECT SIZE(km.mutantResults) FROM KillingMatrix km WHERE km.id = :matrixId")
    int countMutantResultsInMatrix(@Param("matrixId") Long matrixId);
    
    /**
     * Find killing matrix by execution config ID
     */
    @Query("SELECT km FROM KillingMatrix km JOIN ExecConfig ec ON ec.killingMatrix.id = km.id WHERE ec.id = :execConfigId")
    KillingMatrix findByExecConfigId(@Param("execConfigId") Long execConfigId);
}
