package com.example.myspringbootapp.service;

import com.example.myspringbootapp.dto.UserInfoDto;
import com.example.myspringbootapp.dto.UpdateUserInfoDto;
import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService { // 可以合并到之前的 UserService

    @Autowired
    private UserRepository userRepository;

    // 获取用户信息
    @Transactional(readOnly = true) // 读取操作，设置为只读事务可以优化性能
    public UserInfoDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userId + " 的用户"));

        // 手动映射到 DTO
        return mapUserToDto(user);
    }

    // 更新用户信息
    @Transactional // 写入操作，需要事务
    public UserInfoDto updateUserInfo(Long userId, UpdateUserInfoDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + userId + " 的用户"));

        // 更新允许修改的字段
        if (dto.getEmail() != null) {
            // 可以在这里添加邮箱是否已被其他用户使用的校验
            user.setEmail(dto.getEmail());
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        // 更新其他允许的字段...

        User updatedUser = userRepository.save(user);
        return mapUserToDto(updatedUser); // 返回更新后的用户信息 DTO
    }


    // 辅助方法：将 User 实体映射到 UserInfoDto
    public UserInfoDto mapUserToDto(User user) {
        if (user == null) return null;
        return new UserInfoDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getStudentId()
        );
    }

    // (如果需要) 添加获取用户实体的方法，供其他 Service 使用
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }
}
