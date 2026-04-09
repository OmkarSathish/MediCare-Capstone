package com.capstone.healthcare.auth.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private Set<String> roles;
}
