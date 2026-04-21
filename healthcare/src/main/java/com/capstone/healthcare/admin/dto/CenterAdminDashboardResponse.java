package com.capstone.healthcare.admin.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterAdminDashboardResponse {

    // ── Center identity ──────────────────────────────────────────────────────
    private String centerName;

    // ── Headline stats ───────────────────────────────────────────────────────
    private long totalAppointments;
    private long pendingAppointments;
    private long approvedAppointments;
    private long rejectedAppointments;
    private long cancelledAppointments;
    private long assignedTests;

    // ── Revenue ──────────────────────────────────────────────────────────────
    /** Total revenue from APPROVED appointments at this center */
    private double totalRevenue;

    /** { testName → revenue } — top 5 by revenue for this center */
    private Map<String, Double> revenueByTest;

    /** { "YYYY-MM" → revenue } — last 12 months */
    private Map<String, Double> revenueByMonth;

    // ── Charts ───────────────────────────────────────────────────────────────
    /** { "YYYY-MM" → appointmentCount } — last 12 months */
    private Map<String, Long> appointmentsByMonth;

    /** { testName → appointmentCount } — top 5 for this center */
    private Map<String, Long> topTests;
}
