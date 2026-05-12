<template>
    <div class="page-container">
        <header class="page-header">
            <h1>MBTI 问卷</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area">
            <p>请根据你的真实倾向选择以下选项。问卷共 20 题，请全部完成。</p>
            <form @submit.prevent="submitMbti" class="mbti-form">
                <!-- 题目将在这里渲染 -->
                <div v-for="(question, index) in questions" :key="question.id" class="question-block">
                    <p class="question-text"><strong>{{ question.text }}</strong></p>
                    <div class="options">
                        <div v-for="option in question.options" :key="option.value" class="option">
                            <input type="radio" :id="`${question.id}_${option.value}`" :name="question.id"
                                :value="option.value" v-model="answers[question.id]">
                            <label :for="`${question.id}_${option.value}`">{{ option.text }}</label>
                        </div>
                    </div>
                    <!-- 添加分隔线，除了最后一题 -->
                    <hr v-if="index < questions.length - 1" class="question-divider">
                </div>

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
import apiClient from '@/api/axios'; // 引入配置好的 Axios 实例

const router = useRouter();

const getUserId = () => {
    // 尝试从 localStorage 获取 (你需要确保登录时设置了它)
    const userId = localStorage.getItem('user-id');
    if (!userId) {
        console.error("错误：无法获取用户ID。请确保用户已登录并在登录时存储了 user-id。");
        // 在实际应用中，这里可能需要强制跳转到登录页
        // router.push('/login');
        return null; // 返回 null 表示获取失败
    }
    // localStorage 存的是字符串，后端需要 Long/Integer，所以最好转换一下
    const parsedId = parseInt(userId, 10);
    if (isNaN(parsedId)) {
        console.error("错误：存储的用户ID无效。");
        return null;
    }
    return parsedId;
};
// 定义所有问题及其选项和对应的 MBTI 值
const questions = ref([
    // I/E (问题 1-5)
    { id: 'q1', text: '1. 当要和生人打交道时:', options: [{ value: 'I', text: '感到需做出努力' }, { value: 'E', text: '感觉愉快，不费力' }] },
    { id: 'q2', text: '2. 在群体中:', options: [{ value: 'I', text: '愿意安静地默默工作' }, { value: 'E', text: '是个很好的协调者' }] },
    { id: 'q3', text: '3. 你认为自己:', options: [{ value: 'E', text: '比一般人更热情' }, { value: 'I', text: '没有一般人那么易激动' }] },
    { id: 'q4', text: '4. 你可以:', options: [{ value: 'E', text: '很容易和一个人想谈多久就谈多久' }, { value: 'I', text: '只在特定的环境下或对特定的人才会有很多话说' }] },
    { id: 'q5', text: '5. 你倾向拥有:', options: [{ value: 'E', text: '很多认识的人和很亲密的朋友' }, { value: 'I', text: '一些很亲密的朋友和一些认识的人' }] },
    // S/N (问题 6-10)
    { id: 'q6', text: '6. 常常和__相处得很好：', options: [{ value: 'N', text: '喜欢幻想的人' }, { value: 'S', text: '注重现实的人' }] }, // 注意原题号与实际序号不符，这里按顺序编为q6
    { id: 'q7', text: '7. 当和许多人合作时，我倾向于：', options: [{ value: 'S', text: '遵循既定的方式做事' }, { value: 'N', text: '自己创造新的方法' }] },
    { id: 'q8', text: '8. 我对__更感厌烦：', options: [{ value: 'S', text: '奇幻的理论' }, { value: 'N', text: '不喜欢理论的人' }] },
    { id: 'q9', text: '9. 如果你是个教师，你更愿意教:', options: [{ value: 'S', text: '讲实际的课程' }, { value: 'N', text: '讲理论的课程' }] },
    { id: 'q10', text: '10. 当我置身于一段关系中时我倾向相信：', options: [{ value: 'S', text: '若它没有被破坏，别修补它' }, { value: 'N', text: '永远有进步的空间' }] },
    // T/F (问题 11-15)
    { id: 'q11', text: '11. 我更重视：', options: [{ value: 'F', text: '别人的感受' }, { value: 'T', text: '别人的权利' }] }, // 注意原题号与实际序号不符，这里按顺序编为q11
    { id: 'q12', text: '12. 你更喜欢接受哪一种赞扬？', options: [{ value: 'F', text: '你是个感性的人' }, { value: 'T', text: '你是个理性的人' }] },
    { id: 'q13', text: '13. 你认为哪个是对人更大的夸奖：', options: [{ value: 'F', text: '有共同的见地' }, { value: 'T', text: '有洞察力' }] }, // 注意这里原文选项似乎有误，强行按F/T理解
    { id: 'q14', text: '14. 你常常：', options: [{ value: 'F', text: '让你的情感控制了你的思想' }, { value: 'T', text: '用你的思想控制你的情感' }] },
    { id: 'q15', text: '15. 我把大部分和别人的相遇视为：', options: [{ value: 'F', text: '友善及重要的' }, { value: 'T', text: '另有目的' }] },
    // J/P (问题 16-20)
    { id: 'q16', text: '16. 按照时间做事：', options: [{ value: 'J', text: '是我喜欢的方式' }, { value: 'P', text: '对我来说是个束缚' }] }, // 注意原题号与实际序号不符，这里按顺序编为q16
    { id: 'q17', text: '17. 对我来说，适应__较困难：', options: [{ value: 'J', text: '经常性的变化' }, { value: 'P', text: '规定好的程序' }] }, // 注意理解：适应“经常变化”困难对应J，适应“规定程序”困难对应P
    { id: 'q18', text: '18. 当__时，我发挥得更好：', options: [{ value: 'J', text: '仔细订出计划来做事' }, { value: 'P', text: '处理意外突发事件' }] },
    { id: 'q19', text: '19. 当你想起要做某件小事或想起要买某件小东西时，你常常：', options: [{ value: 'J', text: '趁没忘就记在纸上' }, { value: 'P', text: '想过后没多久就忘记了' }] },
    { id: 'q20', text: '20. 我是这类型的人：', options: [{ value: 'J', text: '喜欢在一个时间里专心于一件事情直到完成' }, { value: 'P', text: '享受同时进行好几件事情' }] },
]);

// 使用 reactive 来存储答案，key 是问题 id，value 是选中的选项值 (I/E/S/N/T/F/J/P)
const answers = reactive({});
// 初始化 answers 对象，为每个问题设置一个 null 初始值
questions.value.forEach(q => { answers[q.id] = null; });

const validationError = ref(null); // 用于存储验证错误信息
const isSubmitting = ref(false); // 用于控制提交按钮状态

const submitMbti = async () => {

    isSubmitting.value = true;
    validationError.value = null; // 重置错误信息

    // 1. 验证是否所有问题都已回答
    const unansweredQuestions = questions.value.filter(q => answers[q.id] === null || answers[q.id] === undefined);

    if (unansweredQuestions.length > 0) {
        validationError.value = `您还有 ${unansweredQuestions.length} 题尚未回答，请全部完成后再提交。`;
        isSubmitting.value = false;
        // 滚动到第一个未回答的问题（可选的体验优化）
        const firstUnansweredId = unansweredQuestions[0].id;
        const element = document.getElementById(`${firstUnansweredId}_I`) || document.getElementById(`${firstUnansweredId}_E`) || document.getElementById(`${firstUnansweredId}_S`) || document.getElementById(`${firstUnansweredId}_N`) || document.getElementById(`${firstUnansweredId}_T`) || document.getElementById(`${firstUnansweredId}_F`) || document.getElementById(`${firstUnansweredId}_J`) || document.getElementById(`${firstUnansweredId}_P`);
        if (element) {
            element.closest('.question-block').scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return; // 阻止提交
    }

    // 2. 获取用户 ID
    const userId = getUserId(); // 调用上面定义的函数获取 ID
    if (!userId) {
        validationError.value = "无法获取用户信息，请重新登录后尝试。";
        isSubmitting.value = false;
        return;
    }

    // 3. 构建提交给后端的数据 DTO (只需要 answers)
    const submissionData = {
        answers: { ...answers } // 使用展开运算符复制 reactive 对象
    };

    try {
        // 4. 调用后端 API <--- 新增步骤
        // 请求方法: POST
        // 请求 URL: /api/users/{userId}/mbti (根据你的后端 Controller 定义)
        // 请求体(Body): submissionData (包含答案的 JSON)
        console.log(`向 /api/users/${userId}/mbti 发送数据:`, submissionData); // 调试输出
        const response = await apiClient.post(`/users/${userId}/mbti`, submissionData);

        // 5. 处理后端成功响应 <--- 新增步骤
        const savedResult = response.data; // 后端应该返回保存后的 MbtiResult 对象
        console.log('后端成功响应，MBTI 结果已保存:', savedResult);

        // 从后端返回的结果中获取计算好的 MBTI 类型
        const finalMbti = savedResult.mbtiType;
        if (!finalMbti) {
            console.error("后端响应中缺少 mbtiType 字段");
            validationError.value = "提交成功，但未能获取有效的MBTI结果。";
            isSubmitting.value = false;
            return;
        }


        // 6. 跳转到结果页面 (使用从后端获取的结果)
        router.push({ name: 'MbtiResult', params: { result: finalMbti } });
        // 成功跳转后，当前组件会卸载，isSubmitting 状态会自动消失
    } catch (error) {
        // 7. 处理错误 <--- 新增步骤
        console.error('提交 MBTI 问卷失败:', error);
        isSubmitting.value = false; // 出错时，允许用户重试

        if (error.response) {
            // 请求已发出，服务器用状态码响应 (非 2xx)
            console.error('后端错误响应:', error.response.data);
            // 尝试从后端获取更具体的错误信息
            const backendMessage = error.response.data?.message || error.response.data?.error || '未知服务器错误';
            if (error.response.status === 404) {
                validationError.value = `提交失败: 未找到用户或 API 路径 (${error.response.status})`;
            } else if (error.response.status === 400) {
                validationError.value = `提交失败: 请求数据无效或不完整。${backendMessage ? '(' + backendMessage + ')' : ''}`; // 显示后端验证错误
            } else {
                validationError.value = `提交失败: 服务器错误 (${error.response.status})。${backendMessage}`;
            }
        } else if (error.request) {
            // 请求已发出，但没有收到响应 (网络问题)
            console.error('无响应:', error.request);
            validationError.value = '提交失败: 无法连接到服务器，请检查网络连接或稍后再试。';
        } else {
            // 设置请求时触发了一个错误
            console.error('请求设置错误:', error.message);
            validationError.value = `提交失败: 请求发送过程中发生错误 (${error.message})。`;
        }
    }
    // 注意: finally 块在这里不是必需的，因为成功会跳转，失败会重置 isSubmitting
};
</script>

<style scoped>
/* 复用或调整之前的页面样式 */
.page-container {
    padding: 20px;
    max-width: 800px;
    /* 限制最大宽度以便阅读 */
    margin: 0 auto;
    /* 居中 */
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
}

.mbti-form {
    margin-top: 15px;
}

.question-block {
    margin-bottom: 25px;
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
    /* 选项垂直排列 */
    gap: 10px;
    /* 选项之间的间距 */
}

.option {
    display: flex;
    align-items: center;
    /* 垂直居中对齐 radio 和 label */
    background-color: #f8f9fa;
    /* 轻微背景色 */
    padding: 10px 15px;
    border-radius: 5px;
    border: 1px solid #eee;
    cursor: pointer;
    /* 让整个选项区域可点击 */
    transition: background-color 0.2s ease, border-color 0.2s ease;
}

.option:hover {
    background-color: #e9ecef;
    border-color: #ced4da;
}

.option input[type="radio"] {
    margin-right: 10px;
    /* 使 radio 按钮更大更容易点击 */
    width: 1.1em;
    height: 1.1em;
    cursor: pointer;
}

.option label {
    flex-grow: 1;
    /* 让 label 占据剩余空间 */
    color: #495057;
    cursor: pointer;
}

/* 当 radio 被选中时，给选项添加更明显的样式 */
.option input[type="radio"]:checked+label {
    font-weight: bold;
    color: #0056b3;
}

.option input[type="radio"]:checked {
    /* 可以加一些选中效果 */
}

/* 选中选项的容器样式 */
.option:has(input[type="radio"]:checked) {
    background-color: #e7f1ff;
    /* 选中时的背景色 */
    border-color: #b3d1ff;
}


.question-divider {
    border: none;
    border-top: 1px solid #eee;
    margin-top: 25px;
    /* 分隔线上方间距 */
    margin-bottom: 25px;
    /* 分隔线下方间距 */
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
    /* 独占一行 */
    width: fit-content;
    /* 宽度自适应内容 */
    margin-left: auto;
    /* 尝试右对齐 */
    margin-right: auto;
    /* 居中按钮 */
}

.submit-button:hover:not(:disabled) {
    background-color: #218838;
}

.submit-button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
    opacity: 0.7;
}
</style>