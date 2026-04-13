package com.capstone.healthcare.diagnosticcenter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPriceEntry {
    private int centerId;
    private String centerName;
    private double price;
}
