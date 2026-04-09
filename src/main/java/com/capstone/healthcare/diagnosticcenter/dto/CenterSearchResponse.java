package com.capstone.healthcare.diagnosticcenter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterSearchResponse {

    private int id;
    private String name;
    private String address;
}
