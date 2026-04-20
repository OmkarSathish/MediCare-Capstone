package com.capstone.healthcare.auth.service;

public interface IPasswordEncoderService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
