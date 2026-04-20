package com.capstone.healthcare.diagnosticcenter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestedPriceResponse {
    private double suggestedPrice;
    private String basis; // "average" | "platform_floor"
}
