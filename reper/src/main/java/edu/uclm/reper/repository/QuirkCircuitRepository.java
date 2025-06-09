package edu.uclm.reper.repository;

import edu.uclm.reper.model.QuirkCircuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for QuirkCircuit entity operations
 */
@Repository
public interface QuirkCircuitRepository extends JpaRepository<QuirkCircuit, Long> {
    
    /**
     * Find circuits by number of qubits
     */
    List<QuirkCircuit> findByQbits(int qbits);
    
    /**
     * Find circuits by qubits range
     */
    @Query("SELECT qc FROM QuirkCircuit qc WHERE qc.qbits BETWEEN :minQbits AND :maxQbits")
    List<QuirkCircuit> findByQbitsBetween(@Param("minQbits") int minQbits, @Param("maxQbits") int maxQbits);
    
    /**
     * Find circuits containing specific code patterns
     */
    @Query("SELECT qc FROM QuirkCircuit qc WHERE qc.code LIKE %:codePattern%")
    List<QuirkCircuit> findByCodeContaining(@Param("codePattern") String codePattern);
}
