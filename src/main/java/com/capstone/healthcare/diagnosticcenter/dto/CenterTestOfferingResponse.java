package com.capstone.healthcare.diagnosticcenter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterTestOfferingResponse {

    private int centerId;
    private int testId;
    private String testName;
    private double testPrice;
    private String normalValue;
    private String units;
}
