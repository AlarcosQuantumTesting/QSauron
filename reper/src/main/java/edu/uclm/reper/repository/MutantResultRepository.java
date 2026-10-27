package edu.uclm.reper.repository;

import edu.uclm.reper.model.MutantResult;
import edu.uclm.reper.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MutantResult entity operations
 */
@Repository
public interface MutantResultRepository extends JpaRepository<MutantResult, Long> {

    /**
     * Find mutant results by result type
     */
    List<MutantResult> findByResult(Result result);

    /**
     * Find mutant results by mutant ID
     */
    List<MutantResult> findByMutantId(Long mutantId);

    /**
     * Find mutant results by test case ID
     */
    List<MutantResult> findByTestCaseId(String testCaseId);

    /**
     * Count results by type
     */
    @Query("SELECT COUNT(mr) FROM MutantResult mr WHERE mr.result = :result")
    long countByResult(@Param("result") Result result);

    /**
     * Find killed mutants
     */
    @Query("SELECT mr FROM MutantResult mr WHERE mr.result = 'KILLED'")
    List<MutantResult> findKilledMutants();

    /**
     * Find alive mutants
     */
    @Query("SELECT mr FROM MutantResult mr WHERE mr.result = 'ALIVE'")
    List<MutantResult> findAliveMutants();

    /**
     * Find zombie mutants
     */
    @Query("SELECT mr FROM MutantResult mr WHERE mr.result = 'ZOMBIE'")
    List<MutantResult> findZombieMutants();

    /**
     * Calculate mutation score for a specific mutant cycle
     */
    @Query("SELECT (COUNT(mr) * 1.0 / (SELECT COUNT(m) FROM Mutant m WHERE m.mutantCycle.id = :cycleId)) " +
            "FROM MutantResult mr WHERE mr.result = 'KILLED' AND mr.mutant.mutantCycle.id = :cycleId")
    Double calculateMutationScore(@Param("cycleId") Long cycleId);

    @Query("SELECT r.mutant.id, r.id, r.result FROM MutantResult r WHERE r.mutant.mutantCycle.project.id = :projectId AND r.mutant.mutantCycle.id = :cycleId")
    List<Object[]> findMatrixResults(@Param("projectId") String projectId, @Param("cycleId") int cycleId);

    @Query("SELECT r.id, r.result FROM MutantResult r WHERE r.mutant.id = :mutantId")
    List<Object[]> findByMutantIdLight(@Param("mutantId") int mutantId);
}
