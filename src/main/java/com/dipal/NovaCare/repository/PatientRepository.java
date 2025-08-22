package com.dipal.NovaCare.repository;

import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    boolean existsByUser(User user);
    Optional<Patient> findByUser(User user);
}