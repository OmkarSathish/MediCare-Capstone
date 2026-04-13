package com.capstone.healthcare.diagnosticcenter.model;

import com.capstone.healthcare.diagnostictest.model.DiagnosticTest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "diagnosticcenter_schema", name = "center_test_offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterTestOffering {

    @EmbeddedId
    private CenterTestOfferingKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("centerId")
    @JoinColumn(name = "center_id")
    private DiagnosticCenter center;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("testId")
    @JoinColumn(name = "test_id")
    private DiagnosticTest test;

    @Column(name = "price", nullable = false)
    private double price;
}
