package edu.uclm.reper.repository;

import edu.uclm.reper.model.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TestSuite entity operations
 */
@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, String> {

    /**
     * Find test suites with test cases
     */
    @Query("SELECT ts FROM TestSuite ts WHERE SIZE(ts.testCases) > 0")
    List<TestSuite> findTestSuitesWithTestCases();

    /**
     * Count test cases in a test suite
     */
    @Query("SELECT SIZE(ts.testCases) FROM TestSuite ts WHERE ts.id = :testSuiteId")
    int countTestCasesInTestSuite(Long testSuiteId);
}
