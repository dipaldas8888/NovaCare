package com.dipal.NovaCare.model;



import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String schedule;

    @Column(nullable = false)
    private Integer maxPatientsPerDay;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_file_id")
    private String imageFileId;

    @Column(nullable = true)
    private String qualification;

    @Column(nullable = true)
    private Integer experience;

    @Column(length = 2000)
    private String profileInfo;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

}