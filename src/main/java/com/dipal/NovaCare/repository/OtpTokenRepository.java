package com.dipal.NovaCare.repository;

import com.dipal.NovaCare.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken,Long> {
     Optional<OtpToken> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}
