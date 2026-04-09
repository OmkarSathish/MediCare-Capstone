package com.capstone.healthcare.admin.service.impl;

import com.capstone.healthcare.admin.dto.AdminDashboardResponse;
import com.capstone.healthcare.admin.service.IAdminDashboardService;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.diagnosticcenter.repository.IDiagnosticCenterRepository;
import com.capstone.healthcare.diagnostictest.repository.IDiagnosticTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final IDiagnosticCenterRepository centerRepository;
    private final IDiagnosticTestRepository testRepository;
    private final IAppointmentRepository appointmentRepository;

    @Override
    public AdminDashboardResponse getDashboardSummary() {
        long totalCenters = centerRepository.count();
        long totalTests = testRepository.count();
        long pendingAppointments = appointmentRepository
                .findByApprovalStatus(ApprovalStatus.PENDING).size();
        return AdminDashboardResponse.builder()
                .totalCenters(totalCenters)
                .totalTests(totalTests)
                .pendingAppointments(pendingAppointments)
                .build();
    }
}
