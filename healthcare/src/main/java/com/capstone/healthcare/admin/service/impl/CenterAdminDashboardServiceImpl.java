package com.capstone.healthcare.admin.service.impl;

import com.capstone.healthcare.admin.dto.CenterAdminDashboardResponse;
import com.capstone.healthcare.admin.service.ICenterAdminDashboardService;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CenterAdminDashboardServiceImpl implements ICenterAdminDashboardService {

        private final IAppointmentRepository appointmentRepository;
        private final IDiagnosticCenterRepository centerRepository;

        @Override
        @Transactional(readOnly = true)
        public CenterAdminDashboardResponse getDashboardForCenter(int centerId) {

                DiagnosticCenter center = centerRepository.findById(centerId)
                                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticCenter", "id", centerId));

                // ── Headline stats ────────────────────────────────────────────────────
                long total = appointmentRepository.countByDiagnosticCenter_Id(centerId);
                long pending = appointmentRepository.countByDiagnosticCenter_IdAndApprovalStatus(centerId,
                                ApprovalStatus.PENDING);
                long approved = appointmentRepository.countByDiagnosticCenter_IdAndApprovalStatus(centerId,
                                ApprovalStatus.APPROVED);
                long rejected = appointmentRepository.countByDiagnosticCenter_IdAndApprovalStatus(centerId,
                                ApprovalStatus.REJECTED);
                long cancelled = appointmentRepository.countByDiagnosticCenter_IdAndApprovalStatus(centerId,
                                ApprovalStatus.CANCELLED);
                long assignedTests = center.getTestOfferings().size();

                // ── Appointments by month (last 12) ───────────────────────────────────
                Map<String, Long> rawMonths = appointmentRepository.countByMonthForCenter(centerId).stream()
                                .collect(Collectors.toMap(
                                                r -> String.format("%04d-%02d", (Number) r[0], (Number) r[1]),
                                                r -> (Long) r[2],
                                                (a, b) -> a,
                                                LinkedHashMap::new));

                Map<String, Long> last12Months = rawMonths.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .skip(Math.max(0, rawMonths.size() - 12))
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (a, b) -> a,
                                                LinkedHashMap::new));

                // ── Top 5 tests for this center ───────────────────────────────────────
                Map<String, Long> topTests = appointmentRepository.countByTestForCenter(centerId).stream()
                                .limit(5)
                                .collect(Collectors.toMap(
                                                r -> (String) r[0],
                                                r -> (Long) r[1],
                                                (a, b) -> a,
                                                LinkedHashMap::new));

                // ── Revenue ─────────────────────────────────────────────────────────────
                Double rawRevTotal = appointmentRepository.sumRevenueForCenter(centerId);
                double totalRevenue = rawRevTotal != null ? rawRevTotal : 0.0;

                Map<String, Double> revenueByTest = appointmentRepository.revenueByTestForCenter(centerId).stream()
                                .limit(5)
                                .collect(Collectors.toMap(
                                                r -> (String) r[0],
                                                r -> ((Number) r[1]).doubleValue(),
                                                (a, b) -> a,
                                                LinkedHashMap::new));

                Map<String, Double> rawRevByMonth = appointmentRepository.revenueByMonthForCenter(centerId).stream()
                                .collect(Collectors.toMap(
                                                r -> String.format("%04d-%02d", (Number) r[0], (Number) r[1]),
                                                r -> ((Number) r[2]).doubleValue(),
                                                (a, b) -> a,
                                                LinkedHashMap::new));
                Map<String, Double> revenueByMonth = rawRevByMonth.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .skip(Math.max(0, rawRevByMonth.size() - 12))
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (a, b) -> a,
                                                LinkedHashMap::new));

                return CenterAdminDashboardResponse.builder()
                                .centerName(center.getName())
                                .totalAppointments(total)
                                .pendingAppointments(pending)
                                .approvedAppointments(approved)
                                .rejectedAppointments(rejected)
                                .cancelledAppointments(cancelled)
                                .assignedTests(assignedTests)
                                .appointmentsByMonth(last12Months)
                                .topTests(topTests)
                                .totalRevenue(totalRevenue)
                                .revenueByTest(revenueByTest)
                                .revenueByMonth(revenueByMonth)
                                .build();
        }
}
