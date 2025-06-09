package edu.uclm.reper.repository;

import edu.uclm.reper.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Operator entity operations
 */
@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    
    /**
     * Find operators by name
     */
    List<Operator> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find operators by type
     */
    List<Operator> findByType(String type);
    
    /**
     * Find operators by description
     */
    List<Operator> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Find operator by exact name
     */
    Operator findByName(String name);
    
    /**
     * Find operators by name and type
     */
    List<Operator> findByNameAndType(String name, String type);
}
