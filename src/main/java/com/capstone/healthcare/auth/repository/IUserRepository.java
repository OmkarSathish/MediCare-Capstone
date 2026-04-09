package com.capstone.healthcare.auth.repository;

import com.capstone.healthcare.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserAccount, Integer> {

    /**
     * validateUser — finds a user by email and pre-hashed password.
     * Password comparison (BCrypt) must be done in the service layer;
     * this query matches only on email so the service can then call
     * PasswordEncoder.matches().
     */
    Optional<UserAccount> findByEmail(String email);

    /**
     * Convenience lookup used during token refresh and profile queries.
     */
    boolean existsByEmail(String email);
}
