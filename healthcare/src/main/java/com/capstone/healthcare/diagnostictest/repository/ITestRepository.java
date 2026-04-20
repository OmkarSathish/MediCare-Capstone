package com.capstone.healthcare.diagnostictest.repository;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ITestRepository — bare CRUD access to the diagnostic_tests table,
 * used by ITestService for admin catalog maintenance.
 */
@Repository
public interface ITestRepository extends JpaRepository<DiagnosticTest, Integer> {

    Optional<DiagnosticTest> findByTestName(String testName);

    /**
     * viewAllTest — returns tests whose name contains the given criteria.
     * Pass an empty string to retrieve all tests.
     */
    List<DiagnosticTest> findByTestNameContainingIgnoreCase(String criteria);

    boolean existsByTestName(String testName);
}
