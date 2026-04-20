package com.capstone.healthcare.appointment.dto;

import com.capstone.healthcare.appointment.model.ApprovalStatus;
import com.capstone.healthcare.diagnostictest.dto.TestSearchResponse;
import com.capstone.healthcare.patient.dto.PatientProfileResponse;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDetailResponse {

    private int id;
    private LocalDate appointmentDate;
    private ApprovalStatus approvalStatus;
    private String remarks;
    private String specialRequests;
    private PatientProfileResponse patient;
    private CenterSummary center;
    private Set<TestSearchResponse> diagnosticTests;
    private List<AppointmentStatusResponse.StatusEntry> statusHistory;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CenterSummary {
        private int id;
        private String name;
        private String address;
    }
}
