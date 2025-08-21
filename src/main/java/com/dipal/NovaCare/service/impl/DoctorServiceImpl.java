package com.dipal.NovaCare.service.impl;

import com.dipal.NovaCare.dto.DoctorDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.Doctor;
import com.dipal.NovaCare.repository.DoctorRepository;
import com.dipal.NovaCare.service.DoctorService;
import com.dipal.NovaCare.service.ImageStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final ImageStorageService imageStorageService;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             ImageStorageService imageStorageService) {
        this.doctorRepository = doctorRepository;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public Doctor addDoctor(DoctorDTO dto) {
        Doctor d = new Doctor();
        d.setName(dto.getName());
        d.setSpecialization(dto.getSpecialization());
        d.setContactNumber(dto.getContactNumber());
        d.setEmail(dto.getEmail());
        d.setSchedule(dto.getSchedule());
        d.setMaxPatientsPerDay(dto.getMaxPatientsPerDay());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            var up = imageStorageService.uploadDoctorImage(dto.getImage());
            d.setImageUrl(up.url);
            d.setImageFileId(up.fileId);
        }
        return doctorRepository.save(d);
    }

    @Override
    public List<Doctor> getAllDoctors() { return doctorRepository.findAll(); }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));
    }

    @Override
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    @Override
    public Doctor updateDoctor(Long id, DoctorDTO dto) {
        Doctor d = getDoctorById(id);
        d.setName(dto.getName());
        d.setSpecialization(dto.getSpecialization());
        d.setContactNumber(dto.getContactNumber());
        d.setEmail(dto.getEmail());
        d.setSchedule(dto.getSchedule());
        d.setMaxPatientsPerDay(dto.getMaxPatientsPerDay());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            // delete previous cloud file (if any)
            imageStorageService.deleteByFileId(d.getImageFileId());
            var up = imageStorageService.uploadDoctorImage(dto.getImage());
            d.setImageUrl(up.url);
            d.setImageFileId(up.fileId);
        }
        return doctorRepository.save(d);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor d = getDoctorById(id);
        imageStorageService.deleteByFileId(d.getImageFileId());
        doctorRepository.delete(d);
    }
}
