package com.example.myspringbootapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List; // 用于存储多选结果

@Entity
@Table(name = "dorm_habits_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DormHabitsResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 与 User 建立多对一关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "routine_score", nullable = false)
    private Integer routineScore; // 作息评分

    @Column(name = "hygiene_score", nullable = false)
    private Integer hygieneScore; // 卫生评分

    @Column(name = "social_score", nullable = false)
    private Integer socialScore; // 社交评分

    @Column(name = "preference_factors", columnDefinition = "TEXT") // 存储多选结果，例如用逗号分隔或JSON
    private String preferenceFactors; // 宿舍偏好因素 (e.g., "routine,hygiene")

    @Column(name = "pilot_participation", length = 10) // "yes" or "no"
    private String pilotParticipation; // 是否参与试点

    // 可以选择性地存储原始答案，如果需要的话，但这会使表变复杂
    // @Column(columnDefinition = "TEXT")
    // private String rawAnswers; // 例如存储为 JSON 字符串

    @CreationTimestamp
    @Column(name = "submission_timestamp", updatable = false)
    private LocalDateTime submissionTimestamp;
}
