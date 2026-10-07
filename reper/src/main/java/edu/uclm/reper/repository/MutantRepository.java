package edu.uclm.reper.repository;

import edu.uclm.reper.model.Mutant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

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
    /**
     * Find mutants by quirk circuit ID
     */
    @Query("SELECT m FROM Mutant m WHERE m.quirkCircuit.id = :quirkCircuitId")
    List<Mutant> findByQuirkCircuitId(@Param("quirkCircuitId") String quirkCircuitId);

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

    @Query("SELECT m.mutantCycle.id, COUNT(m) FROM Mutant m WHERE m.mutantCycle.project.id = :projectId GROUP BY m.mutantCycle.id")
    List<Object[]> countByCycle(@Param("projectId") String projectId);

    @Query("SELECT m.id, m.index, m.column, m.row, o.name, m.oracleName, m.mutationDetail FROM Mutant m LEFT JOIN m.operator o WHERE m.mutantCycle.project.id = :projectId AND m.mutantCycle.id = :cycleId ORDER BY m.index")
    List<Object[]> findMatrixRows(@Param("projectId") String projectId, @Param("cycleId") int cycleId);

    @Query("SELECT m FROM Mutant m LEFT JOIN FETCH m.quirkCircuit q LEFT JOIN FETCH q.quirkCircuit WHERE m.mutantCycle.project.id = :projectId AND m.mutantCycle.id = :cycleId AND m.index = :mutantIndex")
    Optional<Mutant> findWithCircuit(@Param("projectId") String projectId, @Param("cycleId") int cycleId, @Param("mutantIndex") int mutantIndex);

    @Query("SELECT m FROM Mutant m LEFT JOIN FETCH m.quirkCircuit q LEFT JOIN FETCH q.quirkCircuit WHERE m.mutantCycle.project.id = :projectId AND m.mutantCycle.id = :cycleId AND m.index >= :fromIndex ORDER BY m.index")
    List<Mutant> findCircuitsPage(@Param("projectId") String projectId, @Param("cycleId") int cycleId, @Param("fromIndex") int fromIndex, Pageable page);

    @Query("SELECT m.mutantCycle.project.id, m.mutantCycle.id, m.index, o.name, m.oracleName, m.mutationDetail FROM Mutant m LEFT JOIN m.operator o WHERE m.mutantCycle.project.id IN :projectIds ORDER BY m.index")
    List<Object[]> findSidebarMutants(@Param("projectIds") List<String> projectIds);
}
