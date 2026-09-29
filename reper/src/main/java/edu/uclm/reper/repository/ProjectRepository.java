package edu.uclm.reper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.dto.ProjectLightSummaryDTO;
import edu.uclm.reper.model.Project;

/**
 * Repository interface for Project entity operations
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.qProgram q LEFT JOIN FETCH q.quirkCircuit WHERE p.id = :id")
    java.util.Optional<Project> findByIdWithQProgram(@Param("id") String id);

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

    @Query(value = """
            SELECT
                p.id AS id,
                p.name AS name,
                (CASE WHEN q.id IS NOT NULL AND (SELECT COUNT(*) FROM qcode qc WHERE qc.q_program_id = q.id) > 0 THEN 1 ELSE 0 END) AS hasGeneratedCode,
                (CASE WHEN (SELECT COUNT(*) FROM mutant_cycles mc WHERE mc.project_id = p.id) > 0 THEN 1 ELSE 0 END) AS hasMutantCycles,
                (CASE WHEN (SELECT COUNT(*) FROM test_suites ts WHERE ts.project_id = p.id) > 0 THEN 1 ELSE 0 END) AS hasTestCases,
                g.generator_type AS generatorType
            FROM projects p
            JOIN user_projects up ON p.id = up.project_id
            LEFT JOIN q_programs q ON p.q_program_id = q.id
            LEFT JOIN generators g ON q.generator_id = g.id
            WHERE up.user_id = :userId
            """, nativeQuery = true)
    List<ProjectLightSummaryDTO> findLightProjectsByUserId(@Param("userId") String userId);

}
