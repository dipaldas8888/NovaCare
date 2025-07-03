package com.dipal.NovaCare.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DoctorDTO {
    private String name;
    private String specialization;
    private String contactNumber;
    private String email;
    private String schedule;
    private Integer maxPatientsPerDay;
    private MultipartFile image;
}