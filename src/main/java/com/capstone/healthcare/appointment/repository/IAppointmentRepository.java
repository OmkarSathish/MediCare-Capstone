package com.capstone.healthcare.appointment.repository;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Integer> {

    /**
     * viewAppointments — all appointments belonging to a patient by their name.
     * Uses Spring Data derived query traversing Appointment → Patient → name.
     */
    Set<Appointment> findByPatient_Name(String patientName);

    /**
     * getAppointmentList — filtered list by center, test name, and status.
     * Passing 0 for status is treated as "any status" at the service layer.
     */
    @Query("""
            SELECT a FROM Appointment a
            JOIN a.diagnosticTests t
            WHERE a.diagnosticCenter.id = :centerId
              AND t.testName = :testName
              AND a.approvalStatus = :status
            """)
    List<Appointment> findByCenterAndTestAndStatus(
            @Param("centerId") int centerId,
            @Param("testName") String testName,
            @Param("status") ApprovalStatus status);

    /**
     * getListOfAppointments — all appointments at a given center by center name.
     */
    @Query("SELECT a FROM Appointment a WHERE a.diagnosticCenter.name = :centerName")
    List<Appointment> findByCenterName(@Param("centerName") String centerName);

    List<Appointment> findByApprovalStatus(ApprovalStatus status);
}
