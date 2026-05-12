package com.example.myspringbootapp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Map;

@Data
public class MbtiSubmissionDto {

    // 前端发送的答案，key 是问题ID (q1, q2...)，value 是选项值 (I, E...)
    @NotEmpty(message = "MBTI 答案不能为空") // 确保至少提交了一些答案
    private Map<String, String> answers;

    // 注意：这里没有 userId，因为我们计划从 URL 路径获取
}
