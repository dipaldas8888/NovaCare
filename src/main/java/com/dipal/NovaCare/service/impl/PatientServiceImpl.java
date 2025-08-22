package com.dipal.NovaCare.service.impl;


import com.dipal.NovaCare.dto.PatientDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.repository.PatientRepository;
import com.dipal.NovaCare.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;


    public PatientServiceImpl(PatientRepository patientRepository, CurrentUserService currentUserService) {
        this.patientRepository = patientRepository;
        this.currentUserService = currentUserService;
    }


    public Patient addPatient(PatientDTO dto) {
        var user = currentUserService.currentUser();

        if (patientRepository.existsByUser(user)) {
            throw new IllegalStateException("Profile already exists");
        }
        Patient p = new Patient();
        p.setUser(user); // ownership link
        p.setName(dto.getName());
        p.setContactNumber(dto.getContactNumber());
        p.setEmail(dto.getEmail());
        p.setAddress(dto.getAddress());
        p.setMedicalHistory(dto.getMedicalHistory());
        return patientRepository.save(p);
    }

    public Patient getMyPatient() {
        var user = currentUserService.currentUser();
        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
    }

    public Patient updateMyPatient(PatientDTO dto) {
        var p = getMyPatient();
        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getContactNumber() != null) p.setContactNumber(dto.getContactNumber());
        if (dto.getEmail() != null) p.setEmail(dto.getEmail());
        if (dto.getAddress() != null) p.setAddress(dto.getAddress());
        if (dto.getMedicalHistory() != null) p.setMedicalHistory(dto.getMedicalHistory());
        return patientRepository.save(p);
    }


    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Patient not found with id: " + id));
    }

    @Override
    public Patient updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = getPatientById(id);
        patient.setName(patientDTO.getName());
        patient.setContactNumber(patientDTO.getContactNumber());
        patient.setEmail(patientDTO.getEmail());
        patient.setAddress(patientDTO.getAddress());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        return patientRepository.save(patient);
    }


    @Override
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
    @Override
    public void addCredits(Long patientId, Double amount) {
        Patient patient = getPatientById(patientId);
        patient.setCredits(patient.getCredits() + amount);
        patientRepository.save(patient);
    }
}
