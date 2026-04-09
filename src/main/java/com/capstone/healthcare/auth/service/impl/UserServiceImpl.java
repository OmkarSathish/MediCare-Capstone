package com.capstone.healthcare.auth.service.impl;

import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.repository.IUserRepository;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.auth.service.IUserService;
import com.capstone.healthcare.shared.exception.ResourceNotFoundException;
import com.capstone.healthcare.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IPasswordEncoderService passwordEncoderService;
    private final IRoleRepository roleRepository;

    @Override
    public UserAccount validateUser(String username, String password) {
        UserAccount user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "email", username));
        if (!passwordEncoderService.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }
        return user;
    }

    @Override
    public UserAccount findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "email", email));
    }

    @Override
    @Transactional
    public UserAccount addUser(UserAccount user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already registered: " + user.getEmail());
        }
        user.setPasswordHash(passwordEncoderService.encode(user.getPasswordHash()));
        user.setStatus("ACTIVE");
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("CUSTOMER").build()));
        user.setRoles(Set.of(customerRole));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserAccount removeUser(UserAccount user) {
        UserAccount existing = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("UserAccount", "id", user.getUserId()));
        existing.setStatus("INACTIVE");
        return userRepository.save(existing);
    }
}
