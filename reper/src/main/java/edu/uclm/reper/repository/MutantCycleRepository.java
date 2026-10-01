package edu.uclm.reper.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.MutantCycle;
import edu.uclm.reper.model.MutantCycleId;

/**
 * Repository interface for MutantCycle entity operations
 */
@Repository
public interface MutantCycleRepository extends JpaRepository<MutantCycle, MutantCycleId> {

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

    @Query("SELECT mc.id, p.id, p.name, mc.name FROM MutantCycle mc JOIN mc.project p WHERE mc.experiment.id = :experimentId ORDER BY mc.date ASC")
    List<Object[]> findCycleHeadersByExperimentId(@Param("experimentId") String experimentId);

    /** Por ciclo: [cycleId, projectId, total, killed, zombie, alive]. Misma regla que Mutant.isKilled/isZombie: KILLED gana a ZOMBIE; sin resultados = no ejecutado. */
    @Query(value = "SELECT t.cycle_id, t.project_id, COUNT(*) AS total, "
            + "SUM(CASE WHEN t.k > 0 THEN 1 ELSE 0 END) AS killed, "
            + "SUM(CASE WHEN t.k = 0 AND t.z > 0 THEN 1 ELSE 0 END) AS zombie, "
            + "SUM(CASE WHEN t.k = 0 AND t.z = 0 AND t.n > 0 THEN 1 ELSE 0 END) AS alive "
            + "FROM (SELECT m.mutant_cycle_id AS cycle_id, m.project_id AS project_id, m.id AS mid, "
            + "  SUM(CASE WHEN mr.result = 'KILLED' THEN 1 ELSE 0 END) AS k, "
            + "  SUM(CASE WHEN mr.result = 'ZOMBIE' THEN 1 ELSE 0 END) AS z, "
            + "  COUNT(mr.mutant_id) AS n "
            + "  FROM mutants m "
            + "  JOIN mutant_cycles mc ON mc.id = m.mutant_cycle_id AND mc.project_id = m.project_id "
            + "  LEFT JOIN mutant_results mr ON mr.mutant_id = m.id "
            + "  WHERE mc.experiment_id = :experimentId "
            + "  GROUP BY m.id, m.mutant_cycle_id, m.project_id) t "
            + "GROUP BY t.cycle_id, t.project_id", nativeQuery = true)
    List<Object[]> countMutantStatesByExperimentId(@Param("experimentId") String experimentId);
}

