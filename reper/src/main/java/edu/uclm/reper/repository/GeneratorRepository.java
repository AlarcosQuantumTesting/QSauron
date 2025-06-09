package edu.uclm.reper.repository;

import edu.uclm.reper.model.Generator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Generator entity operations
 */
@Repository
public interface GeneratorRepository extends JpaRepository<Generator, Long> {
    
    /**
     * Find generators by type (discriminator column)
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = :generatorType")
    List<Generator> findByGeneratorType(@Param("generatorType") Class<? extends Generator> generatorType);
    
    /**
     * Find Genetic generators
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = edu.uclm.reper.model.Genetic")
    List<Generator> findGeneticGenerators();
    
    /**
     * Find Grover generators
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = edu.uclm.reper.model.Grover")
    List<Generator> findGroverGenerators();
    
    /**
     * Find Grenoble generators
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = edu.uclm.reper.model.Grenoble")
    List<Generator> findGrenobleGenerators();
    
    /**
     * Find Matrix generators
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = edu.uclm.reper.model.Matrix")
    List<Generator> findMatrixGenerators();
    
    /**
     * Find GroverAndRudolph generators
     */
    @Query("SELECT g FROM Generator g WHERE TYPE(g) = edu.uclm.reper.model.GroverAndRudolph")
    List<Generator> findGroverAndRudolphGenerators();
}
