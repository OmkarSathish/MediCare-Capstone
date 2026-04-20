package com.capstone.healthcare.auth.service;

import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.shared.security.UserPrincipal;

public interface IAuthTokenService {

    String generateToken(UserAccount user);

    UserPrincipal validateToken(String token);

    String refreshToken(String refreshToken);

    void revokeToken(String refreshToken);
}
