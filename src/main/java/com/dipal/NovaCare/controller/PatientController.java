package com.dipal.NovaCare.controller;


import com.dipal.NovaCare.dto.CreditDTO;
import com.dipal.NovaCare.dto.PatientDTO;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<Patient> addPatient(@RequestBody PatientDTO dto) {
        return new ResponseEntity<>(patientService.addPatient(dto), HttpStatus.CREATED);
    }

    // -
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Patient> me() {
        return ResponseEntity.ok(patientService.getMyPatient());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Patient> updateMe(@RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.updateMyPatient(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.canAccessPatient(#id)")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return new ResponseEntity<>(patientService.getPatientById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.canAccessPatient(#id)")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody PatientDTO dto) {
        return new ResponseEntity<>(patientService.updatePatient(id, dto), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return new ResponseEntity<>(patientService.getAllPatients(), HttpStatus.OK);
    }

    @PostMapping("/{id}/credits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addCredits(@PathVariable Long id, @RequestBody CreditDTO creditDTO) {
        patientService.addCredits(id, creditDTO.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
