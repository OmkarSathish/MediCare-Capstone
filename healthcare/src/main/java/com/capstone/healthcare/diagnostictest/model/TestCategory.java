package com.capstone.healthcare.diagnostictest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "diagnostictest_schema", name = "test_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    private String description;
}
