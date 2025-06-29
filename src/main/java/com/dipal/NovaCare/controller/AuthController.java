package com.dipal.NovaCare.controller;


import com.dipal.NovaCare.dto.ForgotPasswordDTO;
import com.dipal.NovaCare.dto.LoginDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.dto.ResetPasswordDTO;
import com.dipal.NovaCare.model.User;
import com.dipal.NovaCare.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        return new ResponseEntity<>(userService.register(registerDTO), HttpStatus.CREATED);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<String> registerAdmin(
            @RequestBody RegisterDTO registerDTO,
            @RequestParam String secret) {
        return new ResponseEntity<>(
                userService.registerAdmin(registerDTO, secret),
                HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        return new ResponseEntity<>(userService.login(loginDTO), HttpStatus.OK);
    }
    @GetMapping("/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<User> verify(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        // Find user by username and return user info (without password)
        User user = userService.getCurrentUser();
        user.setPassword(null); // Hide password
        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        return userService.forgotPassword(forgotPasswordDTO);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        return userService.resetPassword(resetPasswordDTO);
    }
}
