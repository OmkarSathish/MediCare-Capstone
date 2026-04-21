package com.capstone.healthcare.appointment.repository;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Integer> {

  @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.diagnosticCenter WHERE a.id = :id")
  Optional<Appointment> findByIdWithDetails(@Param("id") int id);

  /**
   * viewAppointments — all appointments belonging to a patient by their username
   * (email), ordered newest-first.
   */
  List<Appointment> findByPatient_UsernameOrderByAppointmentDateDesc(String username);

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

  List<Appointment> findByDiagnosticCenter_Id(int centerId);

  List<Appointment> findByDiagnosticCenter_IdAndApprovalStatus(
      int centerId, ApprovalStatus status);

  /**
   * Find active (PENDING/APPROVED) appointments for a patient on a specific date.
   */
  @Query("""
      SELECT a FROM Appointment a
      WHERE a.patient.id = :patientId
        AND a.appointmentDate = :date
        AND a.approvalStatus IN :statuses
      """)
  List<Appointment> findActiveByPatientAndDate(
      @Param("patientId") int patientId,
      @Param("date") java.time.LocalDate date,
      @Param("statuses") List<ApprovalStatus> statuses);

  // ── Dashboard aggregations ────────────────────────────────────────────────

  /** Total all-time appointments */
  long countBy();

  /** Per-status counts */
  long countByApprovalStatus(ApprovalStatus status);

  /** Appointments grouped by center: [centerName, count] */
  @Query("SELECT a.diagnosticCenter.name, COUNT(a) FROM Appointment a GROUP BY a.diagnosticCenter.name ORDER BY COUNT(a) DESC")
  List<Object[]> countByCenter();

  /** Appointments grouped by year-month: [year, month, count] */
  @Query("SELECT YEAR(a.appointmentDate), MONTH(a.appointmentDate), COUNT(a) FROM Appointment a GROUP BY YEAR(a.appointmentDate), MONTH(a.appointmentDate) ORDER BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)")
  List<Object[]> countByMonth();

  /** Top N most booked tests: [testName, count] */
  @Query("SELECT t.testName, COUNT(a) FROM Appointment a JOIN a.diagnosticTests t GROUP BY t.testName ORDER BY COUNT(a) DESC")
  List<Object[]> countByTest();

  // ── Center-scoped dashboard aggregations ──────────────────────────────────

  /** Total appointments at a given center */
  long countByDiagnosticCenter_Id(int centerId);

  /** Per-status count at a given center */
  long countByDiagnosticCenter_IdAndApprovalStatus(int centerId, ApprovalStatus status);

  /** Appointments by month at a given center: [year, month, count] */
  @Query("SELECT YEAR(a.appointmentDate), MONTH(a.appointmentDate), COUNT(a) FROM Appointment a WHERE a.diagnosticCenter.id = :centerId GROUP BY YEAR(a.appointmentDate), MONTH(a.appointmentDate) ORDER BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)")
  List<Object[]> countByMonthForCenter(@Param("centerId") int centerId);

  /** Top tests booked at a given center: [testName, count] */
  @Query("SELECT t.testName, COUNT(a) FROM Appointment a JOIN a.diagnosticTests t WHERE a.diagnosticCenter.id = :centerId GROUP BY t.testName ORDER BY COUNT(a) DESC")
  List<Object[]> countByTestForCenter(@Param("centerId") int centerId);

  // ── Revenue aggregations (APPROVED appointments only) ─────────────────────

  /**
   * Total platform revenue: sum of all test offering prices for APPROVED appts.
   * Uses the current offering price as the booking price.
   */
  @Query("""
      SELECT COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED'
      """)
  Double sumTotalRevenue();

  /** Revenue per center: [centerName, revenue] */
  @Query("""
      SELECT a.diagnosticCenter.name, COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED'
      GROUP BY a.diagnosticCenter.name
      ORDER BY SUM(o.price) DESC
      """)
  List<Object[]> revenueByCenter();

  /** Revenue per test (platform-wide): [testName, revenue] */
  @Query("""
      SELECT t.testName, COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED'
      GROUP BY t.testName
      ORDER BY SUM(o.price) DESC
      """)
  List<Object[]> revenueByTest();

  /** Revenue per month (platform-wide): [year, month, revenue] */
  @Query("""
      SELECT YEAR(a.appointmentDate), MONTH(a.appointmentDate), COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED'
      GROUP BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)
      ORDER BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)
      """)
  List<Object[]> revenueByMonth();

  /** Total revenue for a specific center: [revenue] */
  @Query("""
      SELECT COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED' AND a.diagnosticCenter.id = :centerId
      """)
  Double sumRevenueForCenter(@Param("centerId") int centerId);

  /** Revenue per test for a specific center: [testName, revenue] */
  @Query("""
      SELECT t.testName, COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED' AND a.diagnosticCenter.id = :centerId
      GROUP BY t.testName
      ORDER BY SUM(o.price) DESC
      """)
  List<Object[]> revenueByTestForCenter(@Param("centerId") int centerId);

  /** Revenue per month for a specific center: [year, month, revenue] */
  @Query("""
      SELECT YEAR(a.appointmentDate), MONTH(a.appointmentDate), COALESCE(SUM(o.price), 0)
      FROM Appointment a
      JOIN a.diagnosticTests t
      JOIN CenterTestOffering o ON o.center = a.diagnosticCenter AND o.test = t
      WHERE a.approvalStatus = 'APPROVED' AND a.diagnosticCenter.id = :centerId
      GROUP BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)
      ORDER BY YEAR(a.appointmentDate), MONTH(a.appointmentDate)
      """)
  List<Object[]> revenueByMonthForCenter(@Param("centerId") int centerId);
}
