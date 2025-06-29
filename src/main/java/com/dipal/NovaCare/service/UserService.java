package com.dipal.NovaCare.service;


import com.dipal.NovaCare.dto.ForgotPasswordDTO;
import com.dipal.NovaCare.dto.LoginDTO;
import com.dipal.NovaCare.dto.RegisterDTO;
import com.dipal.NovaCare.dto.ResetPasswordDTO;
import com.dipal.NovaCare.model.User;
import org.springframework.http.ResponseEntity;

public interface UserService {
    String register(RegisterDTO registerDTO);
    String login(LoginDTO loginDTO);
    User getCurrentUser();

   

    String registerAdmin(RegisterDTO registerDTO, String secret);

    ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO);

    ResponseEntity<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO);
}