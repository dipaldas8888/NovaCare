package com.dipal.NovaCare.service;


import com.dipal.NovaCare.dto.LoginDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.model.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    String register(RegisterDTO registerDTO);
    String login(LoginDTO loginDTO);
    User getCurrentUser();

    ResponseEntity<?> forgotPassword(String email);
    ResponseEntity<?> resetPassword(String email, String otp, String newPassword);

    String registerAdmin(RegisterDTO registerDTO, String secret);
}