package edu.uclm.reper.repository;

import edu.uclm.reper.model.Generator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for Generator entity operations
 */
@Repository
public interface GeneratorRepository extends JpaRepository<Generator, Long> {

  Generator findById(int id);
  
}
