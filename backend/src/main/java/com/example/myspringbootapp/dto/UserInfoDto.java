package com.example.myspringbootapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String studentId;
    // 不包含密码等敏感信息
}
