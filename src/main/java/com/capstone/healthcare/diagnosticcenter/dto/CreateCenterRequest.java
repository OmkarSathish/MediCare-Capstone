package com.capstone.healthcare.diagnosticcenter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCenterRequest {

    @NotBlank
    private String name;

    private String contactNo;

    @NotBlank
    private String address;

    @Email
    private String contactEmail;

    private List<String> servicesOffered;
}
