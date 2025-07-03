package com.dipal.NovaCare.service.impl;


import com.dipal.NovaCare.dto.DoctorDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.Doctor;
import com.dipal.NovaCare.repository.DoctorRepository;
import com.dipal.NovaCare.service.DoctorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Doctor addDoctor(DoctorDTO doctorDTO) {
        Doctor doctor = new Doctor();
        doctor.setName(doctorDTO.getName());
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setContactNumber(doctorDTO.getContactNumber());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setSchedule(doctorDTO.getSchedule());
        doctor.setMaxPatientsPerDay(doctorDTO.getMaxPatientsPerDay());

        if (doctorDTO.getImage() != null && !doctorDTO.getImage().isEmpty()) {
            String fileName = storeFile(doctorDTO.getImage());
            doctor.setImageUrl(fileName);
        }
        return doctorRepository.save(doctor);
    }
    private String storeFile(MultipartFile file) {
        try {
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + "_" +
                    StringUtils.cleanPath(file.getOriginalFilename());

            // Create the file path
            Path targetLocation = Paths.get(uploadDir).resolve(fileName);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store file. Please try again!");
        }
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

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
    public Doctor updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor doctor = getDoctorById(id);
        doctor.setName(doctorDTO.getName());
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setContactNumber(doctorDTO.getContactNumber());
        doctor.setEmail(doctorDTO.getEmail());
        doctor.setSchedule(doctorDTO.getSchedule());
        doctor.setMaxPatientsPerDay(doctorDTO.getMaxPatientsPerDay());
        if (doctorDTO.getImage() != null && !doctorDTO.getImage().isEmpty()) {

            if (doctor.getImageUrl() != null) {
                deleteOldImage(doctor.getImageUrl());
            }

            String fileName = storeFile(doctorDTO.getImage());
            doctor.setImageUrl(fileName);
        }
        return doctorRepository.save(doctor);
    }
    private void deleteOldImage(String imageUrl) {
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting old image: " + e.getMessage());
        }
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}
