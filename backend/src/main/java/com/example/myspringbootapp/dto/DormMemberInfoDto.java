package com.example.myspringbootapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DormMemberInfoDto {
    private Long userId;
    private String username;
    private String name;
    private String studentId;
    private String gender;
    private String mbtiType; // MBTI 结果
    private Integer routineScore; // 宿舍习惯 - 作息分
    private Integer hygieneScore; // 宿舍习惯 - 卫生分
    private Integer socialScore;  // 宿舍习惯 - 社交分
    private String preferenceFactors; // 宿舍偏好
    private String pilotParticipation; // 是否参与试点
    private Integer dormId;
}
