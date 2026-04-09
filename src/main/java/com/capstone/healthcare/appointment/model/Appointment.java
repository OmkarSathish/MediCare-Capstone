package com.capstone.healthcare.appointment.model;

import com.capstone.healthcare.diagnosticcenter.model.DiagnosticCenter;
import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import com.capstone.healthcare.patient.model.Patient;
import com.capstone.healthcare.patient.model.TestResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema = "appointment_schema", name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private int id;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus approvalStatus;

    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private DiagnosticCenter diagnosticCenter;

    @ManyToMany
    @JoinTable(
        schema = "appointment_schema",
        name = "appointment_tests",
        joinColumns = @JoinColumn(name = "appointment_id"),
        inverseJoinColumns = @JoinColumn(name = "test_id")
    )
    @Builder.Default
    private Set<DiagnosticTest> diagnosticTests = new HashSet<>();

    @OneToMany
    @JoinColumn(name = "appointment_id")
    @Builder.Default
    private Set<TestResult> testResult = new HashSet<>();
}
