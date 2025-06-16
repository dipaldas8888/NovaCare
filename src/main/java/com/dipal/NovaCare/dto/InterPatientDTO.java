package com.dipal.NovaCare.dto;

import lombok.Data;

@Data
public class InterPatientDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String message;
}
