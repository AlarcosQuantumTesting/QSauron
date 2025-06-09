package edu.uclm.reper.repository;

import edu.uclm.reper.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TestCase entity operations
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    /**
     * Find test cases by test suite ID
     */
    List<TestCase> findByTestSuiteId(Long testSuiteId);
    
    /**
     * Find test cases by index
     */
    List<TestCase> findByIndex(int index);
    
    /**
     * Find test cases by index range
     */
    @Query("SELECT tc FROM TestCase tc WHERE tc.index BETWEEN :minIndex AND :maxIndex")
    List<TestCase> findByIndexBetween(@Param("minIndex") int minIndex, @Param("maxIndex") int maxIndex);
    
    /**
     * Find the maximum index in a test suite
     */
    @Query("SELECT MAX(tc.index) FROM TestCase tc WHERE tc.testSuite.id = :testSuiteId")
    Integer findMaxIndexByTestSuiteId(@Param("testSuiteId") Long testSuiteId);
}
