package com.capstone.healthcare.patient.repository;

import com.capstone.healthcare.patient.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITestResultRepository extends JpaRepository<TestResult, Integer> {

    /**
     * viewResultsByPatient — finds all test results linked to appointments
     * belonging to the given patient username.
     */
    @Query("""
            SELECT tr FROM TestResult tr
            WHERE tr.appointmentId IN (
                SELECT a.id FROM Appointment a
                WHERE a.patient.username = :username
            )
            """)
    List<TestResult> findByPatientUsername(@Param("username") String username);

    /**
     * getAllTestResult — all results for a patient, same as above.
     */
    default List<TestResult> getAllTestResult(String patientUserName) {
        return findByPatientUsername(patientUserName);
    }

    List<TestResult> findByAppointmentId(int appointmentId);
}
