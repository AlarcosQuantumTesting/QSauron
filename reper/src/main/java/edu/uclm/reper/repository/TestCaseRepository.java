package edu.uclm.reper.repository;

import edu.uclm.reper.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for TestCase entity operations
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

}
