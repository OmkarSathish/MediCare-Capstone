package com.capstone.healthcare.diagnosticcenter.repository;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDiagnosticCenterRepository extends JpaRepository<DiagnosticCenter, Integer> {

        /**
         * getDiagnosticCenter — looks up a center by its exact name.
         */
        Optional<DiagnosticCenter> findByName(String name);

        /**
         * searchCenters — case-insensitive partial name match used for lookup.
         */
        List<DiagnosticCenter> findByNameContainingIgnoreCase(String keyword);

        /**
         * getCentersOfferingTest — finds all centers that offer a given test.
         */
        @Query("SELECT dc FROM DiagnosticCenter dc JOIN dc.testOfferings o WHERE o.test.id = :testId")
        List<DiagnosticCenter> findByTestId(@Param("testId") int testId);

        /**
         * viewTestDetails — checks whether a specific test (by name) is offered
         * at the given center.
         */
        @Query("""
                        SELECT dc FROM DiagnosticCenter dc
                        JOIN dc.testOfferings o
                        WHERE dc.id = :centerId AND o.test.testName = :testName
                        """)
        Optional<DiagnosticCenter> findCenterWithTest(
                        @Param("centerId") int centerId,
                        @Param("testName") String testName);
}
