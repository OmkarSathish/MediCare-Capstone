package com.capstone.healthcare.auth.service.impl;

import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.auth.service.IPasswordResetService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements IPasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int OTP_LENGTH = 6;

    private final JavaMailSender mailSender;
    private final IUserRepository userRepository;
    private final IPasswordEncoderService passwordEncoderService;

    /** email → (otp, expiresAt) */
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private record OtpEntry(String otp, LocalDateTime expiresAt) {
    }

    // ── sendOtp ─────────────────────────────────────────────────────────────

    @Override
    public void sendOtp(String email) {
        // Verify the account exists before sending
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "email", email));

        String otp = generateOtp();
        otpStore.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("MediCare – Password Reset OTP");
        message.setText(
                "Your one-time password (OTP) for resetting your MediCare account password is:\n\n"
                        + "    " + otp + "\n\n"
                        + "This OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes.\n"
                        + "If you did not request a password reset, please ignore this email.\n\n"
                        + "— The MediCare Team");
        mailSender.send(message);
    }

    // ── verifyOtp ────────────────────────────────────────────────────────────

    @Override
    public void verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) {
            throw new ValidationException("No OTP was requested for this email.");
        }
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            throw new ValidationException("OTP has expired. Please request a new one.");
        }
        if (!entry.otp().equals(otp)) {
            throw new ValidationException("Invalid OTP.");
        }
    }

    // ── resetPassword ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        verifyOtp(email, otp);

        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "email", email));

        user.setPasswordHash(passwordEncoderService.encode(newPassword));
        userRepository.save(user);

        // Invalidate the OTP after successful use
        otpStore.remove(email);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", random.nextInt(bound));
    }
}
