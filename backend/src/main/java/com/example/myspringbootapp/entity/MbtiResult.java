package com.example.myspringbootapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mbti_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MbtiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 与 User 建立多对一关系
    @ManyToOne(fetch = FetchType.LAZY) // LAZY 表示延迟加载用户信息，提高性能
    @JoinColumn(name = "user_id", nullable = false) // 外键列名，不允许为空
    private User user;

    @Column(name = "mbti_type", length = 4, nullable = false) // MBTI 类型，如 "ISTJ"
    private String mbtiType;

    @CreationTimestamp // 自动设置创建时间戳
    @Column(name = "submission_timestamp", updatable = false)
    private LocalDateTime submissionTimestamp;

    // 构造函数等由 Lombok @Data, @NoArgsConstructor, @AllArgsConstructor 提供
}