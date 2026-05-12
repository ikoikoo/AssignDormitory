package com.example.myspringbootapp.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String token; // JWT Token
    private String tokenType = "Bearer"; // Token 类型，通常是 Bearer
    private Long id; // 用户 ID
    private String username;
    private String email;
    private List<String> roles;

    public LoginResponseDto(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}