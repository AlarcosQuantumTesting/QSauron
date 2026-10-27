package edu.uclm.reper.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.Operator;



@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {

    Optional<Operator> findByName(String name);
    
}
