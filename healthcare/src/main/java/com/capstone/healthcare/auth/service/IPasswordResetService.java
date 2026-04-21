package com.capstone.healthcare.auth.service;

public interface IPasswordResetService {

    /** Generate a 6-digit OTP for the email, store it, and send it via email. */
    void sendOtp(String email);

    /**
     * Validate the OTP for the email. Throws ValidationException if invalid or
     * expired.
     */
    void verifyOtp(String email, String otp);

    /** Verify the OTP and then update the user's password. */
    void resetPassword(String email, String otp, String newPassword);
}
