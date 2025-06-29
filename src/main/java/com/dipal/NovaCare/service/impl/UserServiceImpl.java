package com.dipal.NovaCare.service.impl;


import com.dipal.NovaCare.dto.ForgotPasswordDTO;
import com.dipal.NovaCare.dto.LoginDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.dto.ResetPasswordDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.PasswordResetToken;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.model.Role;
import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.PasswordResetTokenRepository;
import com.dipal.NovaCare.repository.PatientRepository;
import com.dipal.NovaCare.repository.RoleRepository;
import com.dipal.NovaCare.repository.UserRepository;
import com.dipal.NovaCare.security.JwtTokenProvider;
import com.dipal.NovaCare.service.EmailService;
import com.dipal.NovaCare.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    @Value("${app.admin.secret}")
    private String validAdminSecret;
    private final PatientRepository patientRepository;
    private  final EmailService emailService;

    // Update constructor
    public UserServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           PatientRepository patientRepository,
                           EmailService emailService, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.patientRepository = patientRepository;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;

    }

    @Override
    public String register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        Role roles = roleRepository.findByName("ROLE_USER").orElseThrow(() ->
                new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Role not found"));
        user.setRoles(Collections.singleton(roles));

        userRepository.save(user);

        Patient patient = new Patient();
        patient.setName(user.getUsername());
        patient.setEmail(user.getEmail());
        patient.setContactNumber("");
        patient.setAddress("");
        patient.setMedicalHistory("");
        patient.setUser(user);
        patientRepository.save(patient);
        return "User registered successfully";
    }

    @Override
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsernameOrEmail(),
                        loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtTokenProvider.generateToken(userDetails);
    }

    @Override
    @Transactional
    public String registerAdmin(RegisterDTO registerDTO, String adminSecret) {
        // Verify secret
        if (!validAdminSecret.equals(adminSecret)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Invalid admin creation secret");
        }


        if (!registerDTO.getEmail().endsWith("@hospital.com")) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "Admin emails must use hospital domain");
        }


        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not configured"));
        user.setRoles(Collections.singleton(adminRole));

        userRepository.save(user);

        Patient patient = new Patient();
        patient.setName(user.getUsername());
        patient.setEmail(user.getEmail());
        patient.setContactNumber("");
        patient.setAddress("");
        patient.setMedicalHistory("");
        patient.setUser(user);
        patientRepository.save(patient);

        return "Admin registered successfully";  }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));
    }
    @Override
    @Transactional
    public ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        try {
            User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "User not found"));

            // Delete any existing token
            passwordResetTokenRepository.deleteByUser(user);

            // Generate token
            String token = generateResetToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            // Save token
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(expiryDate);
            passwordResetTokenRepository.save(passwordResetToken);

            // Send email
            String resetLink = "http://your-frontend-url/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            return ResponseEntity.ok()
                    .body(Map.of("message", "Password reset link sent to your email"));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error processing request"));
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

            // Update password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
            userRepository.save(user);

            // Delete used token
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

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}

