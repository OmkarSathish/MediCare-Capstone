package com.capstone.healthcare.diagnostictest.repository;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDiagnosticTestRepository extends JpaRepository<DiagnosticTest, Integer> {

    /**
     * getTestsOfDiagnosticCenter — all active tests offered at a given center.
     */
    @Query("""
            SELECT t FROM DiagnosticTest t
            JOIN t.centerOfferings o
            WHERE o.center.id = :centerId
            """)
    List<DiagnosticTest> findByCenterId(@Param("centerId") int centerId);

    /**
     * viewAllTest — searches by test name containing the criteria string.
     */
    List<DiagnosticTest> findByTestNameContainingIgnoreCase(String criteria);

    Optional<DiagnosticTest> findByTestName(String testName);

    /**
     * getTestsByCategory — all tests belonging to a given category.
     */
    List<DiagnosticTest> findByCategory_CategoryId(int categoryId);
}
