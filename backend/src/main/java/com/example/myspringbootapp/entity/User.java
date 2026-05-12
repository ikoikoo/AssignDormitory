package com.example.myspringbootapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data; // 使用 Lombok 简化 getter/setter 等
import lombok.NoArgsConstructor; // 添加无参构造
import lombok.AllArgsConstructor; // 添加全参构造
@Entity // 标记这是一个 JPA 实体
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"), // 确保用户名唯一
        @UniqueConstraint(columnNames = "email")     // 确保邮箱唯一
})
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id // 标记这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键生成策略：自增
    private Long id;

    @NotBlank // Spring Validation 注解，不允许为空或仅空格
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false) // 密码不能为空
    private String password; // 存储加密后的密码

    @NotBlank
    @Column(unique = true)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(name = "student_id", unique = true, length = 50)
    private String studentId;

    @Column(length = 10)
    private String gender;

    @Column(nullable = false, length = 20) // 角色不能为空，长度限制20
    private String role = "ROLE_USER"; // 默认角色是普通用户 (USER)
}
