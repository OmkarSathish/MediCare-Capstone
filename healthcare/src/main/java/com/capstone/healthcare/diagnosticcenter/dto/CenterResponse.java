package com.capstone.healthcare.diagnosticcenter.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterResponse {

    private int id;
    private String name;
    private String contactNo;
    private String address;
    private String contactEmail;
    private String status;
    private List<String> servicesOffered;
    private Set<TestSummary> tests;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestSummary {
        private int id;
        private String testName;
        private double testPrice;
    }
}
