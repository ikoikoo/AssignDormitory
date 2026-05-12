<template>
    <div class="login-container">
        <h2>学生登录</h2>
        <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
                <label for="username">用户名:</label> <!-- 修改标签文本 -->
                <input type="text" id="username" v-model="username" required>
            </div>
            <div class="form-group">
                <label for="password">密码:</label>
                <input type="password" id="password" v-model="password" required>
            </div>
            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
            <button type="submit" :disabled="loading">
                {{ loading ? '登录中...' : '登录' }}
            </button>
            <p class="register-link">
                还没有账号？ <router-link to="/register">立即注册</router-link>
            </p>
        </form>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import apiClient from '@/api/axios'; // 确认导入

const username = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');
const router = useRouter();
const route = useRoute();

const handleLogin = async () => {
    loading.value = true;
    errorMessage.value = '';
    console.log('尝试登录:', { username: username.value, password: password.value });

    try {
        // --- API 调用 ---
        const response = await apiClient.post('/auth/login', {
            username: username.value,
            password: password.value
        });

        // --- 只有成功时才执行 ---
        const loginInfo = response.data; // loginInfo 在 try 块内定义
        console.log('登录成功，获取信息:', loginInfo);

        // 存储信息
        localStorage.setItem('user-token', loginInfo.token);
        localStorage.setItem('user-id', loginInfo.id.toString());
        localStorage.setItem('user-info', JSON.stringify({
            id: loginInfo.id,
            username: loginInfo.username,
            email: loginInfo.email,
            roles: loginInfo.roles || []
        }));

        // 跳转
        const redirectPath = route.query.redirect || '/dashboard';
        router.replace(redirectPath);

    } catch (error) { // <-- *** 重点检查这个 catch 块内部 ***
        console.error('登录失败:', error); // 打印错误对象
        loading.value = false; // 结束加载

        // 清理 localStorage
        localStorage.removeItem('user-token');
        localStorage.removeItem('user-id');
        localStorage.removeItem('user-info');

        // --- *** 确认这里及之后没有引用 loginInfo *** ---
        if (error.response) {
            console.error('后端错误响应:', error.response.data);
            if (error.response.status === 401) {
                errorMessage.value = '用户名或密码错误，请重试。'; // 不引用 loginInfo
            } else {
                const backendMessage = error.response.data?.message || error.response.data?.error || '未知服务器错误';
                errorMessage.value = `登录失败: ${backendMessage} (${error.response.status})`; // 不引用 loginInfo
            }
        } else if (error.request) {
            errorMessage.value = '无法连接到登录服务器，请检查网络。'; // 不引用 loginInfo
        } else {
            errorMessage.value = `登录请求发送失败: ${error.message}`; // 不引用 loginInfo
        }
        // --- *** 确认 catch 块在这里正确结束 *** ---
    }
    // --- *** 确认 try...catch 之后，函数结束前，没有代码在这里引用 loginInfo *** ---
    // 例如，不应该有类似 console.log(loginInfo) 或 if(loginInfo) ... 的代码在这里
};
</script>

<style scoped>
.login-container {
    max-width: 400px;
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

.login-form .form-group {
    margin-bottom: 20px;
}

.login-form label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
    color: #555;
}

.login-form input[type="text"],
.login-form input[type="password"] {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
    /* 防止 padding 撑大元素 */
}

.login-form button {
    width: 100%;
    padding: 12px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s ease;
}

.login-form button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
}

.login-form button:not(:disabled):hover {
    background-color: #0056b3;
}

.error-message {
    color: #dc3545;
    margin-bottom: 15px;
    text-align: center;
    font-size: 0.9em;
}

.register-link {
    text-align: center;
    margin-top: 20px;
    font-size: 0.9em;
    color: #555;
}

.register-link a {
    color: #007bff;
    text-decoration: none;
    font-weight: bold;
}

.register-link a:hover {
    text-decoration: underline;
}
</style>