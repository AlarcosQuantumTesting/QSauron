package edu.uclm.reper.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.dto.ExperimentListItemDTO;
import edu.uclm.reper.model.Experiment;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, String> {


    @Query("SELECT e FROM Experiment e WHERE e.user.id = :userId AND e.status = 'RUNNING' ORDER BY e.createdAt DESC")
    List<Experiment> findActiveByUserId(@Param("userId") String userId);

    @Query("SELECT new edu.uclm.reper.dto.ExperimentListItemDTO(e.id, e.name, e.status, e.createdAt, e.finishedAt, e.durationSeconds, e.totalCircuits, e.completedCircuits) FROM Experiment e WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    List<ExperimentListItemDTO> findListItemsByUserId(@Param("userId") String userId);

    @Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.execConfig WHERE e.id = :id")
    Optional<Experiment> findByIdWithExecConfig(@Param("id") String id);

    @Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.mutantCycles mc LEFT JOIN FETCH e.execConfig WHERE e.id = :id")
    Optional<Experiment> findByIdWithDetails(@Param("id") String id);
}

