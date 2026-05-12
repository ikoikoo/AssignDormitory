package com.example.myspringbootapp.service;

import com.example.myspringbootapp.entity.User;
import com.example.myspringbootapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList; // 用于创建空的权限列表

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 只读事务
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("未找到用户名为: " + username + " 的用户"));
        // --- 根据用户的 role 字符串创建权限列表 ---
        System.out.println("加载的用户角色 (来自数据库): " + user.getRole());
        // Spring Security 要求权限格式通常是 "ROLE_XYZ"
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole()); // 使用从数据库读到的角色
        List<GrantedAuthority> authorities = Collections.singletonList(authority);
        // 第三个参数是用户的权限列表，这里简化处理，给一个空列表
        System.out.println("创建的权限列表: " + authorities);
        // 如果你有角色/权限系统，需要在这里加载并转换用户的权限
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 必须提供加密后的密码
                true, // isEnabled?
                true, // isAccountNonExpired?
                true, // isCredentialsNonExpired?
                true, // isAccountNonLocked?
                authorities
        );
        // 注意：如果需要更复杂的用户信息封装（例如包含 id），可以创建一个 UserDetails 的实现类
    }
}
