package edu.uclm.reper.repository;

import edu.uclm.reper.model.QCircuit;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
public interface QCircuitRepository extends JpaRepository<QCircuit, Long> {
    
}
