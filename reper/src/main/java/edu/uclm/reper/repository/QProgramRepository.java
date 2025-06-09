package edu.uclm.reper.repository;

import edu.uclm.reper.model.QProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for QProgram entity operations
 */
@Repository
public interface QProgramRepository extends JpaRepository<QProgram, Long> {
    
    /**
     * Find quantum programs by name
     */
    List<QProgram> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find quantum programs by number of qubits
     */
    List<QProgram> findByQubits(int qubits);
    
    /**
     * Find quantum programs by number of qubits range
     */
    @Query("SELECT qp FROM QProgram qp WHERE qp.qubits BETWEEN :minQubits AND :maxQubits")
    List<QProgram> findByQubitsBetween(@Param("minQubits") int minQubits, @Param("maxQubits") int maxQubits);
    
    /**
     * Find quantum programs by shots
     */
    List<QProgram> findByShots(int shots);
    
    /**
     * Find quantum programs with expressions
     */
    @Query("SELECT qp FROM QProgram qp WHERE qp.expressions IS NOT NULL")
    List<QProgram> findProgramsWithExpressions();
}
