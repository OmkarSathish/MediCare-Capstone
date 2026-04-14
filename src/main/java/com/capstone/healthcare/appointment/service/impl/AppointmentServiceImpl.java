package com.capstone.healthcare.appointment.service.impl;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.appointment.service.IAppointmentService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final IAppointmentRepository appointmentRepository;

    private static final int MAX_TESTS_PER_DAY = 4;
    private static final int MAX_CENTERS_PER_DAY = 3;

    @Override
    @Transactional
    public Appointment addAppointment(Appointment appointment) {
        // ── Daily booking limits ──────────────────────────────────────────────
        List<ApprovalStatus> activeStatuses = List.of(ApprovalStatus.PENDING, ApprovalStatus.APPROVED);
        List<Appointment> existing = appointmentRepository.findActiveByPatientAndDate(
                appointment.getPatient().getPatientId(),
                appointment.getAppointmentDate(),
                activeStatuses);

        int existingTestCount = existing.stream()
                .mapToInt(a -> a.getDiagnosticTests().size())
                .sum();
        int newTestCount = appointment.getDiagnosticTests().size();
        if (existingTestCount + newTestCount > MAX_TESTS_PER_DAY) {
            throw new ValidationException(
                    "Daily limit exceeded: you can book at most " + MAX_TESTS_PER_DAY
                            + " tests per day. You already have " + existingTestCount + " test(s) on this date.");
        }

        Set<Integer> centerIds = existing.stream()
                .map(a -> a.getDiagnosticCenter().getId())
                .collect(Collectors.toSet());
        centerIds.add(appointment.getDiagnosticCenter().getId());
        if (centerIds.size() > MAX_CENTERS_PER_DAY) {
            throw new ValidationException(
                    "Daily limit exceeded: you can book at most " + MAX_CENTERS_PER_DAY
                            + " different centers per day.");
        }

        appointment.setApprovalStatus(ApprovalStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentRepository.findById(saved.getId())
                .orElse(saved);
    }

    @Override
    public List<Appointment> viewAppointments(String patientName) {
        return appointmentRepository.findByPatient_NameOrderByAppointmentDateDesc(patientName);
    }

    @Override
    public Appointment viewAppointment(int appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Appointment appointment) {
        appointmentRepository.findById(appointment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointment.getId()));
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAppointmentList(int centerId, String test, int status) {
        boolean hasCenterId = centerId != 0;
        boolean hasTest = test != null && !test.isEmpty();
        ApprovalStatus approvalStatus = switch (status) {
            case 1 -> ApprovalStatus.APPROVED;
            case 2 -> ApprovalStatus.REJECTED;
            case 3 -> ApprovalStatus.CANCELLED;
            case 0 -> ApprovalStatus.PENDING;
            default -> null; // -1 = all statuses
        };

        if (!hasCenterId && !hasTest) {
            // No filters — return by status only (or everything)
            if (approvalStatus == null)
                return appointmentRepository.findAll();
            return appointmentRepository.findByApprovalStatus(approvalStatus);
        }

        if (hasCenterId && !hasTest) {
            // Center filter only
            if (approvalStatus == null)
                return appointmentRepository.findByDiagnosticCenter_Id(centerId);
            return appointmentRepository.findByDiagnosticCenter_IdAndApprovalStatus(centerId, approvalStatus);
        }

        // centerId + testName + status (fall back to PENDING if status unrecognised)
        if (approvalStatus == null)
            approvalStatus = ApprovalStatus.PENDING;
        return appointmentRepository.findByCenterAndTestAndStatus(centerId, test, approvalStatus);
    }

    @Override
    @Transactional
    public Appointment removeAppointment(Appointment appointment) {
        Appointment existing = appointmentRepository.findById(appointment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointment.getId()));
        appointmentRepository.delete(existing);
        return existing;
    }
}
