package edu.uclm.reper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.uclm.reper.model.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
}
