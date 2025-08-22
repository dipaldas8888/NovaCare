package com.dipal.NovaCare.controller;


import com.dipal.NovaCare.dto.DoctorDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.model.Doctor;
import com.dipal.NovaCare.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')" )
    public ResponseEntity<Doctor> addDoctor(@ModelAttribute DoctorDTO doctorDTO) {
        return new ResponseEntity<>(doctorService.addDoctor(doctorDTO), HttpStatus.CREATED);
    }
    // ... existing



    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {

        return new ResponseEntity<>(doctorService.getAllDoctors(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return new ResponseEntity<>(doctorService.getDoctorById(id), HttpStatus.OK);
    }
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Doctor> me() {
        return ResponseEntity.ok(doctorService.getMyDoctor());
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Doctor> updateMe(@ModelAttribute DoctorDTO dto) {
        return ResponseEntity.ok(doctorService.updateMyDoctor(dto));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        return new ResponseEntity<>(
                doctorService.getDoctorsBySpecialization(specialization),
                HttpStatus.OK);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')") // Allow both ADMIN and DOCTOR
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @ModelAttribute DoctorDTO doctorDTO) {
        return new ResponseEntity<>(
                doctorService.updateDoctor(id, doctorDTO),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
