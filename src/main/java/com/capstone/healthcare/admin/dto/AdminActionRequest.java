package com.capstone.healthcare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActionRequest {

    @Positive
    private int targetId;

    @NotBlank
    private String action;

    private String remarks;
}
