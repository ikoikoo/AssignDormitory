package com.example.myspringbootapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // 用于自动设置时间戳

import java.time.LocalDateTime;

@Entity // 标记这是一个 JPA 实体类
@Table(name = "dorm_assignments") // 指定数据库中对应的表名
@Data // Lombok 注解：自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok 注解：生成无参构造函数 (JPA 需要)
@AllArgsConstructor // Lombok 注解：生成包含所有字段的构造函数
public class DormAssignment {

    @Id // 标记这是主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键生成策略：数据库自增
    private Long id; // 分配记录本身的主键 ID

    @ManyToOne(fetch = FetchType.LAZY) // 定义与 User 的多对一关系 (一个 User 对应一条分配记录)
    // FetchType.LAZY 表示默认不立即加载关联的 User 对象，提高性能
    @JoinColumn(name = "user_id",       // 在 dorm_assignments 表中用 user_id 作为外键列
            nullable = false,      // 外键列不允许为空
            unique = true)         // 一个用户只能出现在一条分配记录中 (unique=true 保证)
    private User user; // 关联的用户实体

    @Column(name = "dorm_id", nullable = false) // 分配到的宿舍号/集群号对应的列
    // nullable = false 表示必须有宿舍号
    private Integer dormId; // 存储宿舍/集群的 ID (使用 Integer 类型，因为 K-means 返回的是索引)

    @CreationTimestamp // 当记录创建时，自动填充当前时间戳
    @Column(name = "assignment_timestamp", updatable = false) // 数据库列名，updatable=false 表示创建后不再更新
    private LocalDateTime assignmentTimestamp; // 记录分配执行的时间
    
}
