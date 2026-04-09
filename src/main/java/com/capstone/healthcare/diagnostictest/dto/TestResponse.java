package com.capstone.healthcare.diagnostictest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponse {

    private int id;
    private String testName;
    private double testPrice;
    private String normalValue;
    private String units;
    private String status;
    private String categoryName;
}
