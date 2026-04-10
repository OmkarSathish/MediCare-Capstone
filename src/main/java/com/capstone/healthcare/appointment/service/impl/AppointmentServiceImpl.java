package com.capstone.healthcare.appointment.service.impl;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.appointment.service.IAppointmentService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final IAppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public Appointment addAppointment(Appointment appointment) {
        appointment.setApprovalStatus(ApprovalStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentRepository.findById(saved.getId())
                .orElse(saved);
    }

    @Override
    public Set<Appointment> viewAppointments(String patientName) {
        return appointmentRepository.findByPatient_Name(patientName);
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
