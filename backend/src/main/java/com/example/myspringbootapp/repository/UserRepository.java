package com.example.myspringbootapp.repository;

import com.example.myspringbootapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // 导入 Optional

@Repository // 标记这是一个 Spring 管理的 Repository Bean
public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找用户 (用于登录和加载用户信息)
    Optional<User> findByUsername(String username);

    // 检查用户名是否存在 (用于注册)
    Boolean existsByUsername(String username);

    // 检查邮箱是否存在 (用于注册)
    Boolean existsByEmail(String email);
}