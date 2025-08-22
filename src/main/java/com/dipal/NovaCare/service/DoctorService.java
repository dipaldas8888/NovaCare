package com.dipal.NovaCare.service;


import com.dipal.NovaCare.dto.DoctorDTO;
import com.dipal.NovaCare.model.Doctor;

import java.util.List;

public interface DoctorService {
    Doctor addDoctor(DoctorDTO doctorDTO);
    List<Doctor> getAllDoctors();
    Doctor getDoctorById(Long id);
    List<Doctor> getDoctorsBySpecialization(String specialization);
    Doctor updateDoctor(Long id, DoctorDTO doctorDTO);
    void deleteDoctor(Long id);

    Doctor getMyDoctor();

    Doctor updateMyDoctor(DoctorDTO dto);
}