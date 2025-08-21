package com.dipal.NovaCare.service.impl;

import com.dipal.NovaCare.dto.ForgotPasswordDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.dto.ResetPasswordDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.*;
import com.dipal.NovaCare.repository.*;
import com.dipal.NovaCare.service.EmailService;
import com.dipal.NovaCare.service.UserService;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.doctor.secret}")
    private String validDoctorSecret;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           EmailService emailService,
                           PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }
    @PostConstruct
    public void initAdmin() {
        try {
            // Try to get the user by email first
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(adminEmail);
            // User exists, maybe log info and return
            System.out.println("Admin user already exists: " + adminEmail);
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                // User does not exist, create it
                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                        .setEmail(adminEmail)
                        .setPassword(adminPassword)
                        .setDisplayName("Admin User");
                try {
                    UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                    System.out.println("Admin user created: " + userRecord.getUid());
                } catch (FirebaseAuthException ex) {
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Firebase admin: " + ex.getMessage());
                }
            } else {
                // Other FirebaseAuthException
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Firebase admin: " + e.getMessage());
            }
        }
    }



    @Override
    public String register(RegisterDTO registerDTO) {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                .setEmail(registerDTO.getEmail())
                .setPassword(registerDTO.getPassword());
        UserRecord firebaseUser;
        try {
            firebaseUser = FirebaseAuth.getInstance().createUser(firebaseRequest);
        } catch (FirebaseAuthException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Firebase user: " + e.getMessage());
        }

        User user = new User();
        user.setFirebaseUid(firebaseUser.getUid());
        user.setEmail(registerDTO.getEmail());

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "User role not found"));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        // Create Patient profile
        Patient patient = new Patient();
        patient.setName(registerDTO.getUsername() != null ? registerDTO.getUsername() : ""); // Use provided username or default
        patient.setEmail(user.getEmail());
        patient.setContactNumber("");
        patient.setAddress("");
        patient.setMedicalHistory("");
        patient.setUser(user);
        patient.setCredits(6.0);  // Initialize credits here

        patientRepository.save(patient);

        return "User registered successfully";
    }

    @Override
    public String registerDoctor(RegisterDTO registerDTO, String secret) {
        if (!secret.equals(validDoctorSecret)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Invalid secret for doctor registration");
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        // Create user in Firebase
        UserRecord.CreateRequest firebaseRequest = new UserRecord.CreateRequest()
                .setEmail(registerDTO.getEmail())
                .setPassword(registerDTO.getPassword());
        UserRecord firebaseUser;
        try {
            firebaseUser = FirebaseAuth.getInstance().createUser(firebaseRequest);
        } catch (FirebaseAuthException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Firebase user: " + e.getMessage());
        }

        User user = new User();
        user.setFirebaseUid(firebaseUser.getUid());
        user.setEmail(registerDTO.getEmail());

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseThrow(() -> new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Doctor role not found"));
        user.setRoles(Collections.singleton(doctorRole));

        userRepository.save(user);

        // Create Doctor profile
        Doctor doctor = new Doctor();
        doctor.setName(registerDTO.getUsername() != null ? registerDTO.getUsername() : "");
        doctor.setEmail(user.getEmail());
        doctor.setContactNumber("");
        doctor.setSpecialization("");
        doctor.setSchedule("");
        doctor.setMaxPatientsPerDay(0);
        doctor.setUser(user);
        doctorRepository.save(doctor);

        return "Doctor registered successfully";
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = authentication.getName(); // Firebase UID from token
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    @Transactional
    public ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {

        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            passwordResetTokenRepository.deleteByUser(user);
            passwordResetTokenRepository.flush();

            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

            passwordResetTokenRepository.save(passwordResetToken);

            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email"));

        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing password reset request");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        try {
            if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Passwords don't match");
            }

            PasswordResetToken resetToken = passwordResetTokenRepository
                    .findByToken(resetPasswordDTO.getToken())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Invalid reset token"));

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                passwordResetTokenRepository.delete(resetToken);
                throw new CustomException(HttpStatus.BAD_REQUEST, "Reset token expired");
            }

            User user = resetToken.getUser();
            // Update password in Firebase
            try {
                FirebaseAuth.getInstance().updateUser(
                        new UserRecord.UpdateRequest(user.getFirebaseUid())
                                .setPassword(resetPasswordDTO.getNewPassword())
                );
            } catch (FirebaseAuthException e) {
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Firebase password: " + e.getMessage());
            }
            userRepository.save(user); // Optional, no local password to save

            passwordResetTokenRepository.delete(resetToken);

            return ResponseEntity.ok()
                    .body(Map.of("message", "Password reset successful"));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error processing request"));
        }
    }
}
