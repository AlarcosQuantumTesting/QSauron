package edu.uclm.reper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.BlockCircuit;

@Repository
public interface BlockCircuitRepository extends JpaRepository<BlockCircuit, Long> {
}
