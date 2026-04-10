package com.capstone.healthcare.auth.repository;

import com.capstone.healthcare.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Admin accounts are stored as UserAccount records with a ROLE_ADMIN role.
 * This repository provides admin-specific finders on top of the same table.
 */
@Repository
public interface IAdminRepository extends JpaRepository<UserAccount, Integer> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserAccount> findAllByCenterIdIsNotNull();
}
