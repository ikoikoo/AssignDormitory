package com.example.myspringbootapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200) // 标题不能为空，限制长度
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // 内容不能为空，使用 TEXT 类型存储较长文本
    private String content;

    @CreationTimestamp // 自动设置创建时间戳，作为发布时间
    @Column(name = "publish_date", nullable = false, updatable = false)
    private LocalDateTime publishDate;

    // 可以添加发布者信息，如果需要的话
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "publisher_id")
    // private User publisher;
}