package com.capstone.healthcare.admin.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    // ── Headline stats ───────────────────────────────────────────────────────
    private long totalCenters;
    private long totalTests;
    private long totalPatients;
    private long totalAppointments;
    private long totalCenterAdmins;

    // ── Appointment status breakdown ─────────────────────────────────────────
    private long pendingAppointments;
    private long approvedAppointments;
    private long rejectedAppointments;
    private long cancelledAppointments;

    // ── Revenue ──────────────────────────────────────────────────────────────
    /** Total revenue from all APPROVED appointments */
    private double totalRevenue;

    /** { centerName → revenue } — approved appointments only */
    private Map<String, Double> revenueByCenter;

    /** { testName → revenue } — top 5 by revenue */
    private Map<String, Double> revenueByTest;

    /** { "YYYY-MM" → revenue } — last 12 months */
    private Map<String, Double> revenueByMonth;

    // ── Charts ───────────────────────────────────────────────────────────────
    /** { centerName → appointmentCount } */
    private Map<String, Long> appointmentsByCenter;

    /** { "YYYY-MM" → appointmentCount } — last 12 months */
    private Map<String, Long> appointmentsByMonth;

    /** { testName → appointmentCount } — top 5 */
    private Map<String, Long> topTests;
}
