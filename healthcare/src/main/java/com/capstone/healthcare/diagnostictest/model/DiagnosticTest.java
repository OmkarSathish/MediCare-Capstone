package com.capstone.healthcare.diagnostictest.model;

import com.capstone.healthcare.diagnosticcenter.model.CenterTestOffering;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(schema = "diagnostictest_schema", name = "diagnostic_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private int id;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "test_price", nullable = false)
    private double testPrice;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TestCategory category;

    @OneToMany(mappedBy = "test")
    @Builder.Default
    private Set<CenterTestOffering> centerOfferings = new HashSet<>();
}
