package edu.uclm.reper.repository;

import edu.uclm.reper.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
