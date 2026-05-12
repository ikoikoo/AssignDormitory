<template>
    <div class="page-container">
        <header class="page-header">
            <h1>宿舍习惯问卷</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area">
            <p>为了更好地了解您的生活习惯，请认真完成以下问卷。所有问题均为必填项。</p>
            <form @submit.prevent="submitHabits" class="habits-form">

                <!-- 1. 作息习惯 -->
                <fieldset class="question-group">
                    <legend>1. 作息习惯</legend>
                    <div v-for="question in routineQuestions" :key="question.id" class="question-block">
                        <p class="question-text"><strong>{{ question.text }}</strong></p>
                        <div class="options">
                            <div v-for="option in question.options" :key="option.value" class="option">
                                <input type="radio" :id="`${question.id}_${option.value}`" :name="question.id"
                                    :value="option.value" v-model="answers[question.id]" required />
                                <label :for="`${question.id}_${option.value}`">{{ option.text }}</label>
                            </div>
                        </div>
                        <hr v-if="question.id !== 'q_nap'" class="sub-divider">
                    </div>
                </fieldset>

                <!-- 2. 卫生习惯 -->
                <fieldset class="question-group">
                    <legend>2. 卫生习惯</legend>
                    <div v-for="question in hygieneQuestions" :key="question.id" class="question-block">
                        <p class="question-text"><strong>{{ question.text }}</strong></p>
                        <div class="options">
                            <div v-for="option in question.options" :key="option.value" class="option">
                                <input type="radio" :id="`${question.id}_${option.value}`" :name="question.id"
                                    :value="option.value" v-model="answers[question.id]" required />
                                <label :for="`${question.id}_${option.value}`">{{ option.text }}</label>
                            </div>
                        </div>
                        <hr v-if="question.id !== 'q_cleaning'" class="sub-divider">
                    </div>
                </fieldset>

                <!-- 3. 社交偏好 -->
                <fieldset class="question-group">
                    <legend>3. 社交偏好</legend>
                    <div v-for="question in socialQuestions" :key="question.id" class="question-block">
                        <p class="question-text"><strong>{{ question.text }}</strong></p>
                        <div class="options">
                            <div v-for="option in question.options" :key="option.value" class="option">
                                <input type="radio" :id="`${question.id}_${option.value}`" :name="question.id"
                                    :value="option.value" v-model="answers[question.id]" required />
                                <label :for="`${question.id}_${option.value}`">{{ option.text }}</label>
                            </div>
                        </div>
                        <hr v-if="question.id !== 'q_sharing'" class="sub-divider">
                    </div>
                </fieldset>

                <!-- 4. 宿舍偏好 -->
                <fieldset class="question-group">
                    <legend>4. 宿舍偏好</legend>
                    <!-- 多选问题 -->
                    <div class="question-block">
                        <p class="question-text"><strong>{{ preferenceQuestion.text }}</strong></p>
                        <div class="options checkbox-options">
                            <div v-for="option in preferenceQuestion.options" :key="option.value"
                                class="option checkbox-option">
                                <input type="checkbox" :id="`${preferenceQuestion.id}_${option.value}`"
                                    :value="option.value" v-model="answers[preferenceQuestion.id]" />
                                <label :for="`${preferenceQuestion.id}_${option.value}`">{{ option.text }}</label>
                            </div>
                        </div>
                        <hr class="sub-divider">
                    </div>
                    <!-- 单选问题 -->
                    <div class="question-block">
                        <p class="question-text"><strong>{{ pilotQuestion.text }}</strong></p>
                        <div class="options">
                            <div v-for="option in pilotQuestion.options" :key="option.value" class="option">
                                <input type="radio" :id="`${pilotQuestion.id}_${option.value}`" :name="pilotQuestion.id"
                                    :value="option.value" v-model="answers[pilotQuestion.id]" required />
                                <label :for="`${pilotQuestion.id}_${option.value}`">{{ option.text }}</label>
                            </div>
                        </div>
                    </div>
                </fieldset>

                <!-- 验证错误提示 -->
                <div v-if="validationError" class="error-message">
                    {{ validationError }}
                </div>

                <button type="submit" class="submit-button" :disabled="isSubmitting">
                    {{ isSubmitting ? '正在提交...' : '提交问卷' }}
                </button>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';

// 在需要的地方获取 userId
const getUserId = () => {
    const userId = localStorage.getItem('user-id');
    if (!userId) {
        console.error("错误：无法获取用户ID。请确保用户已登录并在登录时存储了 user-id。");
        return null;
    }
    const parsedId = parseInt(userId, 10);
    if (isNaN(parsedId)) {
        console.error("错误：存储的用户ID无效。");
        return null;
    }
    return parsedId;
};

const router = useRouter();

// --- 问题定义 ---
// 为方便计分，直接在选项中定义分数
// 1. 作息习惯
const routineQuestions = ref([
    {
        id: 'q_sleep_time', text: '您通常几点睡觉？', category: 'routine', options: [
            { value: 'before_22', text: '22:00前', score: 4 },
            { value: '22_to_2330', text: '22:00-23:30', score: 3 },
            { value: '2330_to_1', text: '23:30-1:00', score: 2 },
            { value: 'after_1', text: '1:00后', score: 1 },
        ]
    },
    {
        id: 'q_wake_time', text: '您通常几点起床？', category: 'routine', options: [ // 起床时间不直接计分，但保留问题
            { value: 'before_6', text: '6:00前', score: 0 },
            { value: '6_to_730', text: '6:00-7:30', score: 0 },
            { value: '730_to_9', text: '7:30-9:00', score: 0 },
            { value: 'after_9', text: '9:00后', score: 0 },
        ]
    },
    {
        id: 'q_nap', text: '您午睡的频率是？', category: 'routine', options: [
            { value: 'daily', text: '每天', score: 2 },
            { value: 'often', text: '经常', score: 1 },
            { value: 'occasionally', text: '偶尔', score: 0 },
            { value: 'never', text: '从不', score: 0 }, // 从不计0分
        ]
    },
]);

// 2. 卫生习惯
const hygieneQuestions = ref([
    {
        id: 'q_tidy', text: '您整理个人物品的频率是？', category: 'hygiene', options: [
            { value: 'daily', text: '每天整理', score: 2 },
            { value: 'weekly_2_3', text: '每周2-3次', score: 1 },
            { value: 'monthly_1_2', text: '每月1-2次', score: 0 },
            { value: 'rarely', text: '几乎不整理', score: 0 },
        ]
    },
    {
        id: 'q_cleaning', text: '您参与宿舍公共区域打扫的频率是？', category: 'hygiene', options: [
            { value: 'proactive', text: '主动定期打扫', score: 2 },
            { value: 'on_schedule', text: '按值日表执行', score: 1 },
            { value: 'occasionally', text: '偶尔参与', score: 0 },
            { value: 'rarely', text: '很少参与', score: 0 },
        ]
    },
]);

// 3. 社交偏好
const socialQuestions = ref([
    {
        id: 'q_activities', text: '您希望宿舍内组织集体活动吗？', category: 'social', options: [
            { value: 'very_much', text: '非常希望', score: 2 },
            { value: 'neutral', text: '一般', score: 1 },
            { value: 'no', text: '不希望', score: 0 },
        ]
    },
    {
        id: 'q_visitors', text: '朋友到访宿舍的频率是？', category: 'social', options: [
            { value: 'daily', text: '每天', score: 2 }, // 题目描述访客频率，这里按高频率计高分处理，可调整
            { value: 'weekly_2_3', text: '每周2-3次', score: 1 },
            { value: 'monthly_1_2', text: '每月1-2次', score: 0 },
            { value: 'never', text: '从不', score: 0 },
        ]
    },
    {
        id: 'q_sharing', text: '您对室友使用您物品的态度是？', category: 'social', options: [ // 此题不计分，但保留
            { value: 'casual', text: '随意使用', score: 0 },
            { value: 'ask_first', text: '需提前询问', score: 0 },
            { value: 'refuse', text: '拒绝共用', score: 0 },
        ]
    },
]);

// 4. 宿舍偏好 (不计分，但需回答)
const preferenceQuestion = ref({
    id: 'q_preference_factors', type: 'checkbox', text: '您最重视的宿舍分配因素是？（可多选）', options: [
        { value: 'routine', text: '作息时间一致' },
        { value: 'hygiene', text: '卫生习惯相似' },
        { value: 'personality', text: '性格合拍' },
        { value: 'interest', text: '专业/兴趣相同' },
    ]
});
const pilotQuestion = ref({
    id: 'q_pilot', type: 'radio', text: '您是否愿意参与性格匹配宿舍分配试点？', options: [
        { value: 'yes', text: '愿意' },
        { value: 'no', text: '不愿意' },
    ]
});

// 将所有问题合并到一个数组，方便验证
const allQuestions = [
    ...routineQuestions.value,
    ...hygieneQuestions.value,
    ...socialQuestions.value,
    preferenceQuestion.value, // 注意这里是 ref 本身
    pilotQuestion.value // 注意这里是 ref 本身
];

// --- 答案和提交逻辑 ---
const answers = reactive({});
// 初始化答案
allQuestions.forEach(q => {
    if (q.type === 'checkbox') {
        answers[q.id] = []; // Checkbox 初始值为空数组
    } else {
        answers[q.id] = null; // Radio 初始值为 null
    }
});


const validationError = ref(null);
const isSubmitting = ref(false);

// 查找选项分数的辅助函数
const getScore = (questionId, selectedValue) => {
    const question = allQuestions.find(q => q.id === questionId);
    if (!question || !question.options || question.type === 'checkbox') return 0; // Checkbox 不在此函数计分

    const option = question.options.find(opt => opt.value === selectedValue);
    return option ? (option.score || 0) : 0; // 返回分数，如果选项或分数未定义则为0
};


const submitHabits = async () => {
    isSubmitting.value = true;
    validationError.value = null;

    // 1. 验证所有问题是否已回答
    const unanswered = allQuestions.filter(q => {
        const answer = answers[q.id];
        if (q.type === 'checkbox') {
            return !answer || answer.length === 0; // Checkbox 必须至少选一个
        } else {
            return answer === null || answer === undefined || answer === '';
        }
    });

    if (unanswered.length > 0) {
        validationError.value = `您还有 ${unanswered.length} 个问题尚未回答（包括多选题至少选一项），请全部完成后再提交。`;
        isSubmitting.value = false;
        // 滚动到第一个未回答的问题
        const firstUnansweredId = unanswered[0].id;
        const element = document.querySelector(`input[name="${firstUnansweredId}"], input[id^="${firstUnansweredId}_"]`);
        if (element) {
            element.closest('.question-block, .question-group').scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return;
    }

    /// 2. 获取用户 ID <--- 新增
    const userId = getUserId();
    if (!userId) {
        validationError.value = "无法获取您的用户身份，请尝试重新登录后再提交。";
        isSubmitting.value = false;
        return;
    }

    // 3. 准备提交给后端的数据 <--- 新增
    // 后端 DormHabitsSubmissionDto 需要 q_sleep_time, q_preference_factors (数组) 等字段
    // 前端的 answers 对象应该已经包含了这些 key
    const submissionData = { ...answers };
    // 确保多选结果 q_preference_factors 是一个数组
    if (!Array.isArray(submissionData.q_preference_factors)) {
        console.warn("偏好因素不是数组，尝试修正...");
        submissionData.q_preference_factors = submissionData.q_preference_factors ? [submissionData.q_preference_factors] : []; // 如果不是数组但有值，放入数组；否则空数组
    }


    try {
        // 4. 调用后端 API <--- 新增
        // URL: /api/users/{userId}/dorm-habits
        console.log(`向 /api/users/${userId}/dorm-habits 发送数据:`, submissionData); // 调试输出
        const response = await apiClient.post(`/users/${userId}/dorm-habits`, submissionData);

        // 5. 处理后端成功响应 <--- 新增
        const savedResult = response.data; // 后端返回 DormHabitsResult 对象
        console.log('后端成功响应，宿舍习惯结果已保存:', savedResult);

        // 从后端响应获取分数用于跳转
        const { routineScore, hygieneScore, socialScore } = savedResult;
        if (routineScore === undefined || hygieneScore === undefined || socialScore === undefined) {
            console.error("后端响应中缺少必要的分数字段");
            validationError.value = "提交成功，但未能获取有效的分数结果。";
            isSubmitting.value = false;
            return;
        }

        // 6. 跳转到结果页面 (使用从后端获取的分数)
        router.push({
            name: 'DormHabitsResult',
            query: { // 使用 query 参数传递分数
                routineScore: routineScore,
                hygieneScore: hygieneScore,
                socialScore: socialScore
            }
        });

    } catch (error) {
        // 7. 处理错误 <--- 新增
        console.error('提交宿舍习惯问卷失败:', error);
        isSubmitting.value = false; // 允许重试

        if (error.response) {
            console.error('后端错误响应:', error.response.data);
            const backendMessage = error.response.data?.message || error.response.data?.error || '未知服务器错误';
            if (error.response.status === 404) {
                validationError.value = `提交失败: 未找到用户或 API 路径 (${error.response.status})`;
            } else if (error.response.status === 400) {
                validationError.value = `提交失败: 请求数据无效或不完整。${backendMessage ? '(' + backendMessage + ')' : ''}`;
            } else {
                validationError.value = `提交失败: 服务器错误 (${error.response.status})。${backendMessage}`;
            }
        } else if (error.request) {
            console.error('无响应:', error.request);
            validationError.value = '提交失败: 无法连接到服务器，请检查网络连接或稍后再试。';
        } else {
            console.error('请求设置错误:', error.message);
            validationError.value = `提交失败: 请求发送过程中发生错误 (${error.message})。`;
        }
    }
};

</script>

<style scoped>
/* 复用或调整 MBTI 表单的样式 */
.page-container {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
}

.page-header h1 {
    margin: 0;
    color: #333;
}

.back-link {
    padding: 8px 12px;
    background-color: #6c757d;
    color: white;
    text-decoration: none;
    border-radius: 4px;
    transition: background-color 0.3s;
}

.back-link:hover {
    background-color: #5a6268;
}

.content-area {
    background-color: #fff;
    padding: 25px;
    border-radius: 8px;
    border: 1px solid #eee;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.content-area>p:first-child {
    margin-bottom: 25px;
    color: #555;
    font-size: 0.95em;
}

.habits-form {
    margin-top: 15px;
}

/* Fieldset for grouping questions */
.question-group {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 30px;
    background-color: #fdfdfd;
}

.question-group legend {
    font-size: 1.2em;
    font-weight: bold;
    color: #0056b3;
    padding: 0 10px;
    margin-left: 10px;
    /* 对齐 */
}

.question-block {
    margin-bottom: 20px;
}

.question-text {
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
    line-height: 1.5;
}

.options {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.option {
    display: flex;
    align-items: center;
    background-color: #f8f9fa;
    padding: 10px 15px;
    border-radius: 5px;
    border: 1px solid #eee;
    cursor: pointer;
    transition: background-color 0.2s ease, border-color 0.2s ease;
}

.option:hover {
    background-color: #e9ecef;
    border-color: #ced4da;
}

.option input[type="radio"],
.option input[type="checkbox"] {
    margin-right: 10px;
    width: 1.1em;
    height: 1.1em;
    cursor: pointer;
    flex-shrink: 0;
    /* 防止被压缩 */
}

.option label {
    flex-grow: 1;
    color: #495057;
    cursor: pointer;
}

/* Selected option styles */
.option input[type="radio"]:checked+label,
.option input[type="checkbox"]:checked+label {
    font-weight: bold;
    color: #0056b3;
}

.option:has(input[type="radio"]:checked),
.option:has(input[type="checkbox"]:checked) {
    background-color: #e7f1ff;
    border-color: #b3d1ff;
}

/* Checkbox specific layout */
.checkbox-options {
    /* 可能需要调整布局，比如多列显示 */
    /* display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); */
}

.checkbox-option {
    /* Checkbox选项特定样式 */
}


/* Divider within a fieldset */
.sub-divider {
    border: none;
    border-top: 1px dashed #ccc;
    /* 虚线分隔 */
    margin-top: 20px;
    margin-bottom: 20px;
}


.error-message {
    color: #dc3545;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    padding: 10px 15px;
    border-radius: 4px;
    margin-top: 20px;
    margin-bottom: 15px;
    text-align: center;
}

.submit-button {
    margin-top: 20px;
    padding: 12px 25px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1.1em;
    transition: background-color 0.3s, opacity 0.3s;
    display: block;
    width: fit-content;
    margin-left: auto;
    margin-right: auto;
}

.submit-button:hover:not(:disabled) {
    background-color: #218838;
}

.submit-button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
    opacity: 0.7;
}

/* Remove default fieldset margin/padding if needed */
fieldset {
    margin: 0;
    padding: 0;
    border: none;
    /* Use custom border */
    padding: 20px;
    border: 1px solid #ddd;
    border-radius: 8px;
    margin-bottom: 30px;
    background-color: #fdfdfd;
    /* Reapply padding and border */
}

legend {
    padding: 0 10px;
    margin-left: 10px;
    /* Reapply legend style */
}
</style>