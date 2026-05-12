package com.example.myspringbootapp.repository;

import com.example.myspringbootapp.entity.DormAssignment; // 导入 DormAssignment 实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // 用于标记修改查询 (DELETE/UPDATE)
import org.springframework.data.jpa.repository.Query;     // 用于编写自定义 JPQL 或 SQL 查询
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // 与 @Modifying 一起使用时通常需要事务

import java.util.List;
import java.util.Optional; // 导入 Optional

@Repository // 标记这是一个 Spring 管理的 Repository Bean
public interface DormAssignmentRepository extends JpaRepository<DormAssignment, Long> {
    // 继承 JpaRepository<实体类名, 主键类型>
    // 自动获得方法: save(), saveAll(), findById(), findAll(), deleteById(), count(), existsById() 等

    /**
     * 根据用户 ID 查找对应的宿舍分配记录。
     * Spring Data JPA 会自动根据方法名生成查询。
     *
     * @param userId 用户的 ID
     * @return 包含分配记录的 Optional，如果该用户没有分配记录则为空
     */
    Optional<DormAssignment> findByUser_Id(Long userId);

    List<DormAssignment> findByUser_IdIn(List<Long> userIds);

    @Modifying // 必须添加此注解，表明这是一个数据修改操作 (DELETE/UPDATE)
    @Transactional // 修改操作通常需要在事务中执行
    @Query("DELETE FROM DormAssignment da") // JPQL 语句，da 是 DormAssignment 的别名
    void deleteAllAssignments();

    // 根据宿舍号查找所有分配记录
     List<DormAssignment> findByDormId(Integer dormId);
}
