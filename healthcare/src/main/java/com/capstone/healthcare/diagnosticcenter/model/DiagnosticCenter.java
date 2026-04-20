package com.capstone.healthcare.diagnosticcenter.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(schema = "diagnosticcenter_schema", name = "diagnostic_centers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "center_id")
    private int id;

    @Column(name = "center_name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String contactNo;

    private String address;

    @Column(name = "email")
    private String contactEmail;

    @Column(nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(schema = "diagnosticcenter_schema", name = "center_services", joinColumns = @JoinColumn(name = "center_id"))
    @Column(name = "service")
    @Builder.Default
    private List<String> servicesOffered = new ArrayList<>();

    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CenterTestOffering> testOfferings = new HashSet<>();
}
