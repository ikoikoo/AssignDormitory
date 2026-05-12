package com.example.myspringbootapp.controller;

import com.example.myspringbootapp.dto.UserInfoDto;
import com.example.myspringbootapp.dto.UpdateUserInfoDto;
import com.example.myspringbootapp.entity.User; // 需要 User 实体类
import com.example.myspringbootapp.exception.ResourceNotFoundException; // 或者 jakarta.persistence.EntityNotFoundException
import com.example.myspringbootapp.repository.UserRepository; // 需要 UserRepository
import com.example.myspringbootapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // 可以用来抛出 403
import org.springframework.security.core.Authentication; // <--- 导入 Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <--- 导入 SimpleGrantedAuthority (如果需要检查角色)
import org.springframework.security.core.context.SecurityContextHolder; // <--- 导入 SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails; // <--- 导入 UserDetails
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.myspringbootapp.entity.DormAssignment; // 导入
import com.example.myspringbootapp.repository.DormAssignmentRepository; // 导入

import java.util.Map;
import java.util.Optional; // 导入
import java.util.List;

@RestController // 标记这是一个 RESTful 控制器，返回 JSON 数据
@RequestMapping("/api/users") // 所有请求的基础路径为 /api/users
@CrossOrigin(origins = "http://localhost:5173") // **重要：允许来自前端开发服务器的跨域请求**
public class UserController {

    @Autowired // // 用于 createUser 示例
    private UserRepository userRepository;
    @Autowired // 注入 UserService
    private UserService userService;
    @Autowired
    private DormAssignmentRepository dormAssignmentRepository;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users); // 返回 200 OK 和用户列表
    }

    // 根据 ID 获取单个用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok) // 如果找到，返回 200 OK 和用户
                .orElse(ResponseEntity.notFound().build()); // 如果没找到，返回 404 Not Found
    }

    // 创建新用户
    @PostMapping // 创建用户 (示例)
    public ResponseEntity<UserInfoDto> createUser(@Valid @RequestBody User user) { // 返回 DTO
        // 实际应用中，创建用户逻辑也应该在 Service 中完成，并处理密码加密等
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(userService.mapUserToDto(savedUser)); // 使用映射方法
    }

    // 更新用户 (示例，实际可能需要更复杂的逻辑)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userDetails.getUsername());
                    existingUser.setEmail(userDetails.getEmail());
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build(); // 返回 200 OK 表示成功删除
        } else {
            return ResponseEntity.notFound().build(); // 返回 404 Not Found
        }
    }
    // --- 新增的个人信息端点 ---

    /**
     * 获取指定用户的个人信息 (供前端个人信息页面使用)
     * @param userId 用户ID
     * @return UserInfoDto
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserInfoDto> getUserProfile(@PathVariable Long userId) {
        // --- 获取当前认证信息 ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // <--- 定义 authentication

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 如果没有认证信息或用户是匿名的
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未认证");
        }
        // --- ---

        // --- 获取当前登录用户的用户名和实体 ---
        String currentUsername = null; // <--- 定义 currentUsername
        if (authentication.getPrincipal() instanceof UserDetails) {
            currentUsername = ((UserDetails)authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            currentUsername = (String) authentication.getPrincipal();
        }

        if (currentUsername == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法获取当前用户名");
        }
        // --- 创建一个 final 变量 ---
        final String finalCurrentUsername = currentUsername;
        User currentUser = userRepository.findByUsername(currentUsername)
                // --- 在 Lambda 中使用 final 变量 ---
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法根据用户名找到当前用户实体: " + finalCurrentUsername)); // <-- 使用 finalCurrentUsername


        // --- 执行权限检查 ---
        // 检查请求的 userId 是否与当前登录用户的 ID 匹配，或者当前用户是否是管理员
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")); // 检查是否是管理员

        System.out.println("getUserProfile - 请求 UserID: " + userId);
        System.out.println("getUserProfile - 当前认证用户: " + currentUsername + ", ID: " + currentUser.getId());
        System.out.println("getUserProfile - 当前用户权限: " + authentication.getAuthorities());
        System.out.println("getUserProfile - 是否为管理员: " + isAdmin);

        if (!currentUser.getId().equals(userId) && !isAdmin) { // <-- 现在可以使用 currentUser 和 isAdmin 了
            // 如果 ID 不匹配且不是管理员
            System.out.println("getUserProfile - 权限拒绝！ID 不匹配且非管理员。");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "您无权访问此用户信息");
        }
        System.out.println("getUserProfile - 权限检查通过。");
        // --- 权限检查结束 ---


        // --- 调用 Service 获取信息 (如果权限检查通过) ---
        try {
            UserInfoDto userInfo = userService.getUserInfo(userId); // 这个方法现在可以安全调用了
            return ResponseEntity.ok(userInfo);
        } catch (EntityNotFoundException e) {
            // 如果 UserService 内部的 findById 找不到用户 (理论上不应发生，因为上面检查了 currentUser)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "请求的用户信息不存在: " + e.getMessage(), e);
        } catch (Exception e) {
            // 捕获其他潜在的 Service 层异常
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "获取用户信息时发生内部错误", e);
        }
    }
    @GetMapping("/{userId}/dorm-assignment")
    // 权限控制：确保用户只能查询自己的，或者管理员可以查任意（根据你的 getUserProfile 中的逻辑调整）
    // @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')") // 假设 UserDetails 有 id
    public ResponseEntity<?> getUserDormAssignment(@PathVariable Long userId) {

        // --- 添加权限检查 (与 getUserProfile 类似) ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未认证");
        }
        String currentUsername = null;
        Object principal = authentication.getPrincipal(); // 先获取 principal 对象
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername(); // <--- 从 UserDetails 获取用户名
        } else if (principal instanceof String) {
            currentUsername = (String) principal; // <--- 直接使用 String (如果适用)
        }

        // 添加日志确认提取结果
        System.out.println("UserController - Extracted Username: [" + currentUsername + "]");

        if (currentUsername == null) {
            // 如果 principal 不是预期类型，或者 UserDetails 中 username 为 null
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法从认证信息中提取用户名");
        }

        // 现在用非 null 的 currentUsername 查询
        final String finalUsername = currentUsername;
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    // 如果这里还找不到，说明认证服务和用户仓库之间存在严重不一致
                    System.err.println("严重错误: 认证用户 '" + finalUsername + "' 在数据库中找不到!");
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "已认证用户在数据库中不存在");
                });

        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        System.out.println("getUserDormAssignment - 请求 UserID: " + userId);
        System.out.println("getUserDormAssignment - 当前认证用户: " + currentUsername + ", ID: " + currentUser.getId());
        System.out.println("getUserDormAssignment - 当前用户权限: " + authentication.getAuthorities());
        System.out.println("getUserDormAssignment - 是否为管理员: " + isAdmin);

        if (!currentUser.getId().equals(userId) && !isAdmin) {
            System.out.println("权限拒绝：用户 " + currentUsername + " 试图访问用户 " + userId + " 的宿舍分配信息。");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "您无权访问此信息");
        }

        System.out.println("getUserDormAssignment - 权限检查通过。");
        // --- 权限检查结束 ---


        // --- 查询宿舍分配信息 ---
        Optional<DormAssignment> assignmentOpt = dormAssignmentRepository.findByUser_Id(userId);

        if (assignmentOpt.isPresent()) {
            // 如果找到了分配记录，可以只返回宿舍号，或者返回整个对象
            // return ResponseEntity.ok(assignmentOpt.get()); // 返回整个对象
            return ResponseEntity.ok(Map.of("dormId", assignmentOpt.get().getDormId())); // 只返回宿舍号
        } else {
            // 如果用户还没有被分配
            return ResponseEntity.ok(Map.of("message", "暂无宿舍分配信息")); // 或者返回 204 No Content
            // return ResponseEntity.noContent().build();
        }
    }
    // --- ---
    /**
     * 更新指定用户的个人信息
     * @param userId 用户ID
     * @param updateUserInfoDto 包含要更新信息的 DTO
     * @return 更新后的 UserInfoDto
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserInfoDto> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserInfoDto updateUserInfoDto) { // 启用验证
        try {
            UserInfoDto updatedUserInfo = userService.updateUserInfo(userId, updateUserInfoDto);
            return ResponseEntity.ok(updatedUserInfo);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } // 可以添加处理 ConstraintViolationException (验证失败) 的逻辑 -> 400 Bad Request
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "更新用户信息时出错", e);
        }
    }
}
