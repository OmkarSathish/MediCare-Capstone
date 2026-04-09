package com.capstone.healthcare.appointment.dto;

import com.capstone.healthcare.appointment.model.ApprovalStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentStatusResponse {

    private int appointmentId;
    private ApprovalStatus currentStatus;
    private List<StatusEntry> history;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusEntry {
        private ApprovalStatus previousStatus;
        private ApprovalStatus newStatus;
        private String changedBy;
        private LocalDateTime changedAt;
        private String comments;
    }
}
