package edu.uclm.reper.repository;

import edu.uclm.reper.model.Expression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Expression entity operations
 */
@Repository
public interface ExpressionRepository extends JpaRepository<Expression, Long> {
    
    /**
     * Find expressions by name
     */
    List<Expression> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find expressions by type
     */
    List<Expression> findByType(String type);
    
    /**
     * Find expressions by description
     */
    List<Expression> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Find expressions containing specific expression patterns
     */
    @Query("SELECT e FROM Expression e WHERE e.expr LIKE %:pattern%")
    List<Expression> findByExpressionContaining(@Param("pattern") String pattern);
    
    /**
     * Find expressions by name and type
     */
    List<Expression> findByNameAndType(String name, String type);
}
