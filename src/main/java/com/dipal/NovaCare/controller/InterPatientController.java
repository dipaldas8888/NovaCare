package com.dipal.NovaCare.controller;

import com.dipal.NovaCare.dto.InterPatientDTO;
import com.dipal.NovaCare.model.InterEnquiry;
import com.dipal.NovaCare.service.InterPatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class InterPatientController {

    private final InterPatientService service;

    public InterPatientController(InterPatientService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InterEnquiry> sendMessage(@RequestBody InterPatientDTO dto) {
        return new ResponseEntity<>(service.saveMessage(dto), HttpStatus.CREATED);
    }
}
