package com.dipal.NovaCare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class InterEnquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false, length = 500)
    private String message;
}
