package com.dipal.NovaCare.dto;


import lombok.Data;

@Data
public class LoginDTO {
    private String usernameOrEmail;
    private String password;
}
