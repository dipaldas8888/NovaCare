package com.dipal.NovaCare.service.impl;


import com.dipal.NovaCare.dto.LoginDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.exception.CustomException;
import com.dipal.NovaCare.model.OtpToken;
import com.dipal.NovaCare.model.Patient;
import com.dipal.NovaCare.model.Role;
import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.repository.OtpTokenRepository;
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
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.admin.secret}")
    private String validAdminSecret;
    private final PatientRepository patientRepository;
    private final OtpTokenRepository otpTokenRepository;
    private  final EmailService emailService;

    // Update constructor
    public UserServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           PatientRepository patientRepository,OtpTokenRepository otpTokenRepository,
                           EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.patientRepository = patientRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
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
    public ResponseEntity<?> forgotPassword(String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "Email not found"));
        }


        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);


        otpTokenRepository.deleteByEmail(email);


        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiryTime(expiry);
        otpTokenRepository.save(token);


        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "fail", "message", "Failed to send OTP"));
        }

        return ResponseEntity.ok(Map.of("status", "success", "message", "OTP sent to email"));
    }

    @Override
    @Transactional
    public ResponseEntity<?> resetPassword(String email, String otp, String newPassword) {
        var tokenOpt = otpTokenRepository.findByEmailAndOtp(email, otp);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "Invalid OTP"));
        }

        OtpToken token = tokenOpt.get();
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "OTP expired"));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "User not found"));
        }

        var user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpTokenRepository.delete(token);

        return ResponseEntity.ok(Map.of("status", "success", "message", "Password reset successfully"));
    }
}

