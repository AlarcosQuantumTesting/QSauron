package edu.uclm.reper.repository;

import edu.uclm.reper.model.ExecConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Repository interface for ExecConfig entity operations
 */
@Repository
public interface ExecConfigRepository extends JpaRepository<ExecConfig, String> {

    /**
     * Find execution configurations by machine
     */
    List<ExecConfig> findByMachine(String machine);

    /**
     * Find execution configurations by algorithm
     */
    List<ExecConfig> findByExecAlgorithm(String execAlgorithm);

    /**
     * Find execution configurations by execution date
     */
    List<ExecConfig> findByExecutionDate(Date executionDate);

    /**
     * Find execution configurations between dates
     */
    @Query("SELECT ec FROM ExecConfig ec WHERE ec.executionDate BETWEEN :startDate AND :endDate")
    List<ExecConfig> findByExecutionDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Find execution configurations ordered by execution date (most recent first)
     */
    List<ExecConfig> findAllByOrderByExecutionDateDesc();

    /**
     * Find execution configurations by machine and algorithm
     */
    List<ExecConfig> findByMachineAndExecAlgorithm(String machine, String execAlgorithm);

    /**
     * Find execution configurations by mutant cycle ID
     */
    @Query("SELECT ec FROM ExecConfig ec WHERE ec.mutantCycle.id = :mutantCycleId AND ec.mutantCycle.project.id = :projectId")
    ExecConfig findByMutantCycleId(@Param("mutantCycleId") int mutantCycleId, @Param("projectId") String projectId);

    /**
     * Count configurations by machine
     */
    long countByMachine(String machine);

    /**
     * Count configurations by algorithm
     */
    long countByExecAlgorithm(String execAlgorithm);
}
