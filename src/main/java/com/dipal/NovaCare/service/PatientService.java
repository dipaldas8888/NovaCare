package com.dipal.NovaCare.service;


import com.dipal.NovaCare.dto.PatientDTO;
import com.dipal.NovaCare.model.Patient;

import java.util.List;

public interface PatientService {
    Patient addPatient(PatientDTO patientDTO);
    List<Patient> getAllPatients();
    Patient getPatientById(Long id);
    Patient updatePatient(Long id, PatientDTO patientDTO);
    void deletePatient(Long id);
    void addCredits(Long patientId, Double amount);

    Patient updateMyPatient(PatientDTO dto);

    Patient getMyPatient();
}
