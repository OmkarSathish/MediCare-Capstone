package com.capstone.healthcare.appointment.service;

import com.capstone.healthcare.appointment.model.Appointment;
import com.capstone.healthcare.appointment.model.ApprovalStatus;

public interface IAppointmentWorkflowService {

    Appointment approveAppointment(int appointmentId, String adminUsername, String remarks);

    Appointment rejectAppointment(int appointmentId, String adminUsername, String remarks);

    boolean canTransition(ApprovalStatus current, ApprovalStatus next);
}
