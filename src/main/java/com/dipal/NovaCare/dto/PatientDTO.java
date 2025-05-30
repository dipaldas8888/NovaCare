package com.dipal.NovaCare.dto;


import lombok.Data;

@Data
public class PatientDTO {
    private String name;
    private String contactNumber;
    private String email;
    private String address;
    private String medicalHistory;
}
