package edu.uclm.reper.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.MutantCycle;

/**
 * Repository interface for MutantCycle entity operations
 */
@Repository
public interface MutantCycleRepository extends JpaRepository<MutantCycle, Integer> {
    
    /**
     * Find mutant cycles by project ID
     */
    List<MutantCycle> findByProjectId(String projectId);
    
    /**
     * Find mutant cycles by date
     */
    List<MutantCycle> findByDate(Date date);
    
    /**
     * Find mutant cycles between dates
     */
    @Query("SELECT mc FROM MutantCycle mc WHERE mc.date BETWEEN :startDate AND :endDate")
    List<MutantCycle> findByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Find mutant cycles ordered by date (most recent first)
     */
    List<MutantCycle> findAllByOrderByDateDesc();
    
    /**
     * Find mutant cycles for a project ordered by date
     */
    List<MutantCycle> findByProjectIdOrderByDateDesc(String projectId);
    
    /**
     * Find the latest mutant cycle for a project
     */
    @Query("SELECT mc FROM MutantCycle mc WHERE mc.project.id = :projectId ORDER BY mc.date DESC")
    List<MutantCycle> findLatestByProjectId(@Param("projectId") String projectId);
    
    /**
     * Count mutant cycles by project
     */
    long countByProjectId(String projectId);
    
    /**
     * Find mutant cycles with mutants
     */
    @Query("SELECT mc FROM MutantCycle mc WHERE SIZE(mc.mutants) > 0")
    List<MutantCycle> findCyclesWithMutants();
}
