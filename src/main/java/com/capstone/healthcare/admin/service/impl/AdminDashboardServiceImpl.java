package com.capstone.healthcare.admin.service.impl;

import com.capstone.healthcare.admin.dto.AdminDashboardResponse;
import com.capstone.healthcare.admin.service.IAdminDashboardService;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.auth.repository.IAdminRepository;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import com.capstone.healthcare.patient.repository.IPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final IDiagnosticCenterRepository centerRepository;
    private final IDiagnosticTestRepository testRepository;
    private final IAppointmentRepository appointmentRepository;
    private final IPatientRepository patientRepository;
    private final IAdminRepository adminRepository;

    @Override
    public AdminDashboardResponse getDashboardSummary() {

        // ── Headline counts ──────────────────────────────────────────────────
        long totalCenters = centerRepository.count();
        long totalTests = testRepository.count();
        long totalPatients = patientRepository.count();
        long totalAppointments = appointmentRepository.countBy();
        long totalCenterAdmins = adminRepository.findAllByCenterIdIsNotNull().size();

        // ── Status breakdown ─────────────────────────────────────────────────
        long pending = appointmentRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approved = appointmentRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejected = appointmentRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        long cancelled = appointmentRepository.countByApprovalStatus(ApprovalStatus.CANCELLED);

        // ── Appointments by center ───────────────────────────────────────────
        Map<String, Long> byCenter = appointmentRepository.countByCenter().stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1],
                        (a, b) -> a,
                        LinkedHashMap::new));

        // ── Appointments by month (last 12 months) ───────────────────────────
        Map<String, Long> byMonth = appointmentRepository.countByMonth().stream()
                .collect(Collectors.toMap(
                        r -> String.format("%04d-%02d", (Number) r[0], (Number) r[1]),
                        r -> (Long) r[2],
                        (a, b) -> a,
                        LinkedHashMap::new));

        // Keep only the last 12 months, preserving order
        Map<String, Long> last12Months = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .skip(Math.max(0, byMonth.size() - 12))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));

        // ── Top 5 tests ──────────────────────────────────────────────────────
        List<Object[]> rawTests = appointmentRepository.countByTest();
        Map<String, Long> topTests = rawTests.stream()
                .limit(5)
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1],
                        (a, b) -> a,
                        LinkedHashMap::new));

        return AdminDashboardResponse.builder()
                .totalCenters(totalCenters)
                .totalTests(totalTests)
                .totalPatients(totalPatients)
                .totalAppointments(totalAppointments)
                .totalCenterAdmins(totalCenterAdmins)
                .pendingAppointments(pending)
                .approvedAppointments(approved)
                .rejectedAppointments(rejected)
                .cancelledAppointments(cancelled)
                .appointmentsByCenter(byCenter)
                .appointmentsByMonth(last12Months)
                .topTests(topTests)
                .build();
    }
}
