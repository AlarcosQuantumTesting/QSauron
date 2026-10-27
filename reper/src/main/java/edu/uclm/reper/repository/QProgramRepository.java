package edu.uclm.reper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.QProgram;

/**
 * Repository interface for QProgram entity operations
 */
@Repository
public interface QProgramRepository extends JpaRepository<QProgram, String> {
    
}
