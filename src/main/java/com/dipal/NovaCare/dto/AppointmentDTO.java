package com.dipal.NovaCare.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long patientId;
    private Long doctorId;
    private LocalDateTime appointmentDateTime;
    private String notes;
}
