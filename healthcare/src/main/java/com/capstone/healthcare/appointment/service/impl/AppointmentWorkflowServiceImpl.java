package com.capstone.healthcare.appointment.service.impl;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentRepository;
import com.capstone.healthcare.appointment.service.IAppointmentStatusService;
import com.capstone.healthcare.appointment.service.IAppointmentWorkflowService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentWorkflowServiceImpl implements IAppointmentWorkflowService {

        private final IAppointmentRepository appointmentRepository;
        private final IAppointmentStatusService appointmentStatusService;

        @Override
        @Transactional
        public Appointment approveAppointment(int appointmentId, String adminUsername, String remarks) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
                if (!canTransition(appointment.getApprovalStatus(), ApprovalStatus.APPROVED)) {
                        throw new ValidationException(
                                        "Cannot approve appointment in status: " + appointment.getApprovalStatus());
                }
                ApprovalStatus previous = appointment.getApprovalStatus();
                appointment.setApprovalStatus(ApprovalStatus.APPROVED);
                appointment.setRemarks(remarks);
                Appointment saved = appointmentRepository.save(appointment);
                appointmentStatusService.recordStatusChange(
                                appointmentId, previous, ApprovalStatus.APPROVED, adminUsername, remarks);
                return saved;
        }

        @Override
        @Transactional
        public Appointment rejectAppointment(int appointmentId, String adminUsername, String remarks) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
                if (!canTransition(appointment.getApprovalStatus(), ApprovalStatus.REJECTED)) {
                        throw new ValidationException(
                                        "Cannot reject appointment in status: " + appointment.getApprovalStatus());
                }
                ApprovalStatus previous = appointment.getApprovalStatus();
                appointment.setApprovalStatus(ApprovalStatus.REJECTED);
                appointment.setRemarks(remarks);
                Appointment saved = appointmentRepository.save(appointment);
                appointmentStatusService.recordStatusChange(
                                appointmentId, previous, ApprovalStatus.REJECTED, adminUsername, remarks);
                return saved;
        }

        @Override
        @Transactional
        public Appointment cancelAppointment(int appointmentId, String username) {
                Appointment appointment = appointmentRepository.findById(appointmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
                if (appointment.getApprovalStatus() == ApprovalStatus.REJECTED ||
                                appointment.getApprovalStatus() == ApprovalStatus.CANCELLED) {
                        throw new ValidationException(
                                        "Cannot cancel appointment in status: " + appointment.getApprovalStatus());
                }
                ApprovalStatus previous = appointment.getApprovalStatus();
                appointment.setApprovalStatus(ApprovalStatus.CANCELLED);
                Appointment saved = appointmentRepository.save(appointment);
                appointmentStatusService.recordStatusChange(
                                appointmentId, previous, ApprovalStatus.CANCELLED, username, "Cancelled by patient");
                return saved;
        }

        @Override
        public boolean canTransition(ApprovalStatus current, ApprovalStatus next) {
                return current == ApprovalStatus.PENDING
                                && (next == ApprovalStatus.APPROVED || next == ApprovalStatus.REJECTED);
        }
}
