package com.capstone.healthcare.diagnostictest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "diagnostictest_schema", name = "test_parameters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameter_id")
    private int parameterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private DiagnosticTest test;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName;

    @Column(name = "normal_range")
    private String normalRange;

    private String unit;
}
