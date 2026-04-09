package com.capstone.healthcare.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalCenters;
    private long totalTests;
    private long pendingAppointments;
}
