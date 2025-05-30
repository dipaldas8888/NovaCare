package com.dipal.NovaCare.repository;


import com.dipal.NovaCare.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
