package com.example.myspringbootapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class DormHabitsSubmissionDto {

    // 对应前端 answers 对象的 key
    // 添加 @NotNull 或 @NotEmpty 以确保前端发送了这些字段

    @NotNull(message = "睡眠时间不能为空")
    private String q_sleep_time;

    @NotNull(message = "起床时间不能为空")
    private String q_wake_time; // 虽然不计分，但需要提交

    @NotNull(message = "午睡频率不能为空")
    private String q_nap;

    @NotNull(message = "整理频率不能为空")
    private String q_tidy;

    @NotNull(message = "打扫频率不能为空")
    private String q_cleaning;

    @NotNull(message = "集体活动期望不能为空")
    private String q_activities;

    @NotNull(message = "访客频率不能为空")
    private String q_visitors;

    @NotNull(message = "物品使用态度不能为空")
    private String q_sharing; // 虽然不计分，但需要提交

    @NotEmpty(message = "宿舍偏好因素至少选择一项") // 至少选一个
    private List<String> q_preference_factors; // 多选，对应 checkbox 的 value 数组

    @NotNull(message = "是否参与试点不能为空")
    private String q_pilot; // 单选 "yes" or "no"
}
