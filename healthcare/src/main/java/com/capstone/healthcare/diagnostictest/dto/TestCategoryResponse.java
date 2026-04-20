package com.capstone.healthcare.diagnostictest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCategoryResponse {

    private int categoryId;
    private String categoryName;
    private String description;
}
