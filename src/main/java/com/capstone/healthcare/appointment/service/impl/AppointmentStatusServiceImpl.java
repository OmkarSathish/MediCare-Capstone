package com.capstone.healthcare.appointment.service.impl;

import com.capstone.healthcare.appointment.model.AppointmentStatusHistory;
import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.appointment.repository.IAppointmentStatusHistoryRepository;
import com.capstone.healthcare.appointment.service.IAppointmentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentStatusServiceImpl implements IAppointmentStatusService {

    private final IAppointmentStatusHistoryRepository historyRepository;

    @Override
    @Transactional
    public void recordStatusChange(int appointmentId, ApprovalStatus previous, ApprovalStatus next,
            String changedBy, String comments) {
        AppointmentStatusHistory entry = AppointmentStatusHistory.builder()
                .appointmentId(appointmentId)
                .previousStatus(previous)
                .newStatus(next)
                .changedBy(changedBy)
                .comments(comments)
                .build();
        historyRepository.save(entry);
    }

    @Override
    public List<AppointmentStatusHistory> getStatusHistory(int appointmentId) {
        return historyRepository.findByAppointmentIdOrderByChangedAtAsc(appointmentId);
    }
}
