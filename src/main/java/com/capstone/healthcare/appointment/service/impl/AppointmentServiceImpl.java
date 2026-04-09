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
        // When no centerId/testName filter, use efficient single-column queries
        if (centerId == 0 && (test == null || test.isEmpty())) {
            if (status == -1) {
                return appointmentRepository.findAll();
            }
            ApprovalStatus approvalStatus = switch (status) {
                case 1 -> ApprovalStatus.APPROVED;
                case 2 -> ApprovalStatus.REJECTED;
                default -> ApprovalStatus.PENDING;
            };
            return appointmentRepository.findByApprovalStatus(approvalStatus);
        }
        // Full filter: centerId + testName + status
        ApprovalStatus approvalStatus = switch (status) {
            case 1 -> ApprovalStatus.APPROVED;
            case 2 -> ApprovalStatus.REJECTED;
            default -> ApprovalStatus.PENDING;
        };
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
