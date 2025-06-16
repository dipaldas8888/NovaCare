package com.dipal.NovaCare.service.impl;

import com.dipal.NovaCare.dto.InterPatientDTO;
import com.dipal.NovaCare.model.InterEnquiry;
import com.dipal.NovaCare.repository.InterPatientRepository;
import com.dipal.NovaCare.service.InterPatientService;
import org.springframework.stereotype.Service;

@Service
public class InterPatientServiceImp implements InterPatientService {

    private final InterPatientRepository repository;

    public InterPatientServiceImp(InterPatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public InterEnquiry saveMessage(InterPatientDTO dto) {
        InterEnquiry message = new InterEnquiry();
        message.setFirstName(dto.getFirstName());
        message.setLastName(dto.getLastName());
        message.setEmail(dto.getEmail());
        message.setPhone(dto.getPhone());
        message.setCountry(dto.getCountry());
        message.setMessage(dto.getMessage());
        return repository.save(message);
    }
}
