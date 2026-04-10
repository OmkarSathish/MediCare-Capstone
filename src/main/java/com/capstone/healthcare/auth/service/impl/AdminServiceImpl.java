package com.capstone.healthcare.auth.service.impl;

import com.capstone.healthcare.auth.model.Role;
import com.capstone.healthcare.auth.model.UserAccount;
import com.capstone.healthcare.auth.repository.IAdminRepository;
import com.capstone.healthcare.auth.repository.IRoleRepository;
import com.capstone.healthcare.auth.service.IAdminService;
import com.capstone.healthcare.auth.service.IPasswordEncoderService;
import com.capstone.healthcare.shared.exception.ValidationException;
import com.capstone.healthcare.shared.security.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final IAdminRepository adminRepository;
    private final IPasswordEncoderService passwordEncoderService;
    private final IRoleRepository roleRepository;

    @Override
    @Transactional
    public void registerAdmin(String username, String password) {
        if (adminRepository.existsByEmail(username)) {
            throw new ValidationException("Admin already exists with email: " + username);
        }
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").build()));
        UserAccount admin = UserAccount.builder()
                .email(username)
                .fullName("Admin")
                .passwordHash(passwordEncoderService.encode(password))
                .status("ACTIVE")
                .roles(Set.of(adminRole))
                .build();
        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void registerCenterAdmin(String email, String password, String fullName, int centerId) {
        if (adminRepository.existsByEmail(email)) {
            throw new ValidationException("User already exists with email: " + email);
        }
        Role centerAdminRole = roleRepository.findByRoleName("CENTER_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("CENTER_ADMIN").build()));
        UserAccount centerAdmin = UserAccount.builder()
                .email(email)
                .fullName(fullName)
                .passwordHash(passwordEncoderService.encode(password))
                .status("ACTIVE")
                .centerId(centerId)
                .roles(Set.of(centerAdminRole))
                .build();
        adminRepository.save(centerAdmin);
    }

    @Override
    public List<UserAccount> listCenterAdmins() {
        return adminRepository.findAllByCenterIdIsNotNull();
    }

    @Override
    @Transactional
    public void removeCenterAdmin(int userId) {
        UserAccount user = adminRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));
        user.getRoles().removeIf(r -> r.getRoleName().equals(RoleConstants.CENTER_ADMIN));
        user.setCenterId(null);
        adminRepository.save(user);
    }
}
