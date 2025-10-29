package edu.uclm.reper.repository;

import edu.uclm.reper.model.Mutant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Mutant entity operations
 */
@Repository
public interface MutantRepository extends JpaRepository<Mutant, Long> {
    
    /**
     * Find mutants by index
     */
    List<Mutant> findByIndex(int index);
    
    /**
     * Find mutants by column position
     */
    List<Mutant> findByColumn(int column);
    
    /**
     * Find mutants by row position
     */
    List<Mutant> findByRow(int row);
    
    /**
     * Find mutants by mutant cycle ID
     */
    List<Mutant> findByMutantCycleId(Long mutantCycleId);
    
    /**
     * Find mutants by operator ID
     */
    List<Mutant> findByOperatorId(Long operatorId);
    
    /**
     * Find mutants by quirk circuit ID
     */
    List<Mutant> findByQuirkCircuitId(String quirkCircuitId);
    
    /**
     * Find mutants by position (column and row)
     */
    @Query("SELECT m FROM Mutant m WHERE m.column = :column AND m.row = :row")
    List<Mutant> findByPosition(@Param("column") int column, @Param("row") int row);
    
    /**
     * Find mutants within a position range
     */
    @Query("SELECT m FROM Mutant m WHERE m.column BETWEEN :minColumn AND :maxColumn AND m.row BETWEEN :minRow AND :maxRow")
    List<Mutant> findByPositionRange(@Param("minColumn") int minColumn, @Param("maxColumn") int maxColumn, 
                                   @Param("minRow") int minRow, @Param("maxRow") int maxRow);
}
