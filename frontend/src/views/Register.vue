<template>
    <div class="register-container">
        <h2>新用户注册</h2>
        <form @submit.prevent="handleRegister" class="register-form">
            <div class="form-group">
                <label for="username">用户名:</label>
                <input type="text" id="username" v-model="formData.username" required>
                <small>长度 3-20 位</small>
            </div>
            <div class="form-group">
                <label for="email">邮箱:</label>
                <input type="email" id="email" v-model="formData.email" required>
            </div>
            <div class="form-group">
                <label for="password">密码:</label>
                <input type="password" id="password" v-model="formData.password" required>
                <small>长度 6-40 位</small>
            </div>
            <div class="form-group">
                <label for="confirmPassword">确认密码:</label>
                <input type="password" id="confirmPassword" v-model="confirmPassword" required>
            </div>
            <!-- 可选字段 -->
            <div class="form-group">
                <label for="name">姓名:</label>
                <input type="text" id="name" v-model="formData.name">
            </div>
            <div class="form-group">
                <label for="studentId">学号:</label>
                <input type="text" id="studentId" v-model="formData.studentId">
            </div>
            <div class="form-group">
                <label for="gender">性别:</label>
                <select id="gender" v-model="formData.gender">
                    <option value="">请选择</option>
                    <option value="男">男</option>
                    <option value="女">女</option>
                    <option value="其他">其他</option>
                </select>
            </div>

            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
            <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

            <button type="submit" :disabled="loading">
                {{ loading ? '注册中...' : '注册' }}
            </button>
            <p class="login-link">
                已有账号？ <router-link to="/login">直接登录</router-link>
            </p>
        </form>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';

const formData = reactive({
    username: '',
    email: '',
    password: '',
    name: '',       // 初始化可选字段
    studentId: '',
    gender: ''
});
const confirmPassword = ref('');
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const router = useRouter();

const handleRegister = async () => {
    loading.value = true;
    errorMessage.value = '';
    successMessage.value = '';

    // 前端密码确认验证
    if (formData.password !== confirmPassword.value) {
        errorMessage.value = '两次输入的密码不一致！';
        loading.value = false;
        return;
    }

    // 准备提交给后端的数据 (可以过滤掉空的 gender)
    const registerData = { ...formData };
    if (registerData.gender === '') {
        delete registerData.gender; // 如果未选择，不发送 gender 字段
    }

    try {
        // --- 调用后端注册 API ---
        console.log('尝试注册:', registerData);
        const response = await apiClient.post('/auth/register', registerData);

        // --- 处理注册成功 ---
        console.log('注册成功，响应:', response.data);
        successMessage.value = '注册成功！即将跳转到登录页面...'; // 显示成功消息

        // 清空表单 (可选)
        Object.keys(formData).forEach(key => formData[key] = '');
        confirmPassword.value = '';

        // 延时几秒后跳转到登录页
        setTimeout(() => {
            router.push('/login');
        }, 2000); // 2秒后跳转

    } catch (error) {
        // --- 处理注册失败 ---
        loading.value = false;
        console.error('注册失败:', error);

        if (error.response) {
            console.error('后端错误响应:', error.response.data);
            const backendMessage = error.response.data?.message || error.response.data?.error || '注册失败，请检查输入';
            if (error.response.status === 409) { // Conflict (用户名或邮箱已存在)
                errorMessage.value = `注册失败: ${backendMessage}`;
            } else if (error.response.status === 400) { // Bad Request (验证失败)
                // 尝试提取更详细的验证错误 (如果后端在 errors 字段中返回了)
                let detailedErrors = '';
                if (error.response.data?.errors && Array.isArray(error.response.data.errors)) {
                    detailedErrors = error.response.data.errors.map(e => e.defaultMessage || `${e.field} 无效`).join('; ');
                }
                errorMessage.value = `注册失败: 输入数据无效。${detailedErrors || backendMessage}`;
            }
            else {
                errorMessage.value = `注册失败: 服务器错误 (${error.response.status})。`;
            }
        } else if (error.request) {
            errorMessage.value = '注册失败: 无法连接到服务器。';
        } else {
            errorMessage.value = `注册失败: ${error.message}`;
        }
    }
    // 成功时会在延时后跳转，失败时重置 loading
    // if (!successMessage.value) { // 仅在失败时重置 loading，成功时让按钮保持禁用直到跳转
    //    loading.value = false;
    // }
};
</script>

<style scoped>
.register-container {
    max-width: 450px;
    /* 可以比登录框稍宽 */
    margin: 50px auto;
    padding: 30px;
    border: 1px solid #ccc;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    background-color: #fff;
}

h2 {
    text-align: center;
    margin-bottom: 25px;
    color: #333;
}

.register-form .form-group {
    margin-bottom: 15px;
}

/* 减少一点间距 */
.register-form label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
    color: #555;
}

.register-form input[type="text"],
.register-form input[type="email"],
.register-form input[type="password"],
.register-form select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 1em;
}

.register-form small {
    display: block;
    font-size: 0.8em;
    color: #888;
    margin-top: 3px;
}


.register-form button {
    width: 100%;
    padding: 12px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s ease;
    margin-top: 10px;
}

.register-form button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
}

.register-form button:not(:disabled):hover {
    background-color: #218838;
}

.error-message,
.success-message {
    padding: 10px;
    margin-bottom: 15px;
    border-radius: 4px;
    text-align: center;
    font-size: 0.9em;
}

.error-message {
    color: #721c24;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
}

.success-message {
    color: #155724;
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
}

.login-link {
    text-align: center;
    margin-top: 20px;
    font-size: 0.9em;
    color: #555;
}

.login-link a {
    color: #007bff;
    text-decoration: none;
    font-weight: bold;
}

.login-link a:hover {
    text-decoration: underline;
}
</style>