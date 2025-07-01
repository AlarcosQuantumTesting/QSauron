package edu.uclm.reper.repository;

import edu.uclm.reper.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    
}
