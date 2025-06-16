package com.dipal.NovaCare.service;

import com.dipal.NovaCare.dto.InterPatientDTO;
import com.dipal.NovaCare.model.InterEnquiry;

public interface InterPatientService {
    InterEnquiry saveMessage(InterPatientDTO dto);

}
