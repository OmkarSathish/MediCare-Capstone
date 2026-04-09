package com.capstone.healthcare.appointment.service;

import com.capstone.healthcare.appointment.model.AppointmentStatusHistory;
import com.capstone.healthcare.appointment.model.ApprovalStatus;

import java.util.List;

public interface IAppointmentStatusService {

    void recordStatusChange(int appointmentId, ApprovalStatus previous, ApprovalStatus next,
            String changedBy, String comments);

    List<AppointmentStatusHistory> getStatusHistory(int appointmentId);
}
