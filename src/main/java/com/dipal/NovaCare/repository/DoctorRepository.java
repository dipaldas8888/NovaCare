package com.dipal.NovaCare.repository;


import com.dipal.NovaCare.model.Doctor;
import com.dipal.NovaCare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    boolean existsByUser(User user);
    List<Doctor> findBySpecialization(String specialization);
}
