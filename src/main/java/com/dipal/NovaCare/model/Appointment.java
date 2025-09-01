package com.dipal.NovaCare.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(nullable = false)
    private String status; // Pending, Accepted, Completed, Cancelled, Rejected

    @Column
    private String notes;

    @Column(nullable = false)
    private String type;

    @Column(nullable = true)
    private String patientName;

    @Column(nullable = true)
    private String patientMobile;


    @Column
    private String videoSessionId;
}