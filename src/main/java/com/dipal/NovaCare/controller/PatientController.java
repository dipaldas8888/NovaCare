package com.dipal.NovaCare.controller;


import com.dipal.NovaCare.dto.PatientDTO;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Patient> addPatient(@RequestBody PatientDTO patientDTO) {
        return new ResponseEntity<>(
                patientService.addPatient(patientDTO),
                HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return new ResponseEntity<>(patientService.getAllPatients(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return new ResponseEntity<>(
                patientService.getPatientById(id),
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDTO patientDTO) {
        return new ResponseEntity<>(
                patientService.updatePatient(id, patientDTO),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
