package com.dipal.NovaCare.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long doctorId;
    private Long patientId; // Optional, as current user is patient
    private LocalDateTime appointmentDateTime;
    private String notes;
    private String type;
}
