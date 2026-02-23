package edu.uclm.reper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.Project;

/**
 * Repository interface for Project entity operations
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * Find projects by name containing the given string (case insensitive)
     */
    List<Project> findByNameContainingIgnoreCase(String name);

    /**
     * Find projects with mutant cycles
     */
    @Query("SELECT p FROM Project p WHERE SIZE(p.mutantCycles) > 0")
    List<Project> findProjectsWithMutantCycles();

    /**
     * Find projects by user (assuming many-to-many relationship with User)
     */
    @Query("SELECT p FROM Project p JOIN p.users u WHERE u.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") String userId);

    @Query("SELECT p.id, p.name, q.generator FROM Project p JOIN p.users u JOIN p.qProgram q WHERE u.id = :userId")
    List<Object> findProjectsNameByUserId(@Param("userId") String userId);

    @Query("SELECT new edu.uclm.reper.dto.ProjectLightSummaryDTO(" +
            "p.id, " +
            "p.name, " +
            "(CASE WHEN (q IS NOT NULL AND SIZE(q.qCodes) > 0) THEN true ELSE false END), " +
            "(CASE WHEN SIZE(p.mutantCycles) > 0 THEN true ELSE false END), " +
            "(CASE WHEN SIZE(p.testSuites) > 0 THEN true ELSE false END)) " +
            "FROM Project p " +
            "JOIN p.users u " +
            "LEFT JOIN p.qProgram q " +
            "WHERE u.id = :userId")
    List<edu.uclm.reper.dto.ProjectLightSummaryDTO> findLightProjectsByUserId(@Param("userId") String userId);

}
