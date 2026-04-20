package com.capstone.healthcare.diagnostictest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSearchResponse {

    private int id;
    private String testName;
    private double testPrice;
}
