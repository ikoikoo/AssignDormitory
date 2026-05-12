<template>
    <div class="page-container">
        <header class="page-header">
            <h1>个人信息</h1>
            <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
        </header>
        <div class="content-area">
            <div v-if="loading" class="loading-message">加载中...</div>
            <div v-else-if="fetchError" class="error-message">{{ fetchError }}</div>
            <div v-else-if="userInfo" class="profile-details">

                <div class="profile-item">
                    <label>用户名:</label>
                    <span>{{ userInfo.username }}</span>
                </div>
                <div class="profile-item">
                    <label>学号:</label>
                    <span>{{ userInfo.studentId }}</span>
                </div>

                <div class="profile-item editable">
                    <label for="profile-name">姓名:</label>
                    <span v-if="!isEditing">{{ userInfo.name || '未填写' }}</span>
                    <input v-else type="text" id="profile-name" v-model="editableUserInfo.name">
                </div>

                <div class="profile-item editable">
                    <label for="profile-email">邮箱:</label>
                    <span v-if="!isEditing">{{ userInfo.email || '未填写' }}</span>
                    <input v-else type="email" id="profile-email" v-model="editableUserInfo.email">
                </div>

                <div class="profile-item">
                    <label>宿舍分配:</label>
                    <span v-if="loadingDorm">查询中...</span>
                    <span v-else-if="dormError" style="color: red;">{{ dormError }}</span>
                    <span v-else-if="dormAssignment && dormAssignment.dormId">
                        宿舍号 {{ dormAssignment.dormId }}
                    </span>
                    <span v-else-if="dormAssignment && dormAssignment.message">
                        {{ dormAssignment.message }} <!-- 显示 "暂无分配信息" 等 -->
                    </span>
                    <span v-else>
                        暂无分配信息
                    </span>
                </div>

                <div class="profile-actions">
                    <button v-if="!isEditing" @click="toggleEdit(true)" class="edit-button">编辑信息</button>
                    <template v-else>
                        <button @click="saveProfile" :disabled="isSaving" class="save-button">
                            {{ isSaving ? '保存中...' : '保存更改' }}
                        </button>
                        <button @click="toggleEdit(false)" :disabled="isSaving" class="cancel-button">取消</button>
                    </template>
                </div>

                <!-- 更新反馈信息 -->
                <div v-if="updateError" class="error-message update-feedback">{{ updateError }}</div>
                <div v-if="successMessage" class="success-message update-feedback">{{ successMessage }}</div>

            </div>
            <div v-else class="no-data-message">无法加载用户信息。</div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import apiClient from '@/api/axios';

// --- State ---
const userInfo = ref(null); // 存储从后端获取的完整信息 (用于展示)
const editableUserInfo = reactive({ // 用于表单编辑
    name: '',
    email: ''
    // 添加其他可编辑字段的初始值
});
const loading = ref(true); // 初始加载状态
const fetchError = ref(null); // 获取信息时的错误
const isEditing = ref(false); // 是否处于编辑模式
const isSaving = ref(false); // 是否正在保存更新
const updateError = ref(null); // 更新信息时的错误
const successMessage = ref(null); // 更新成功提示

const dormAssignment = ref(null); // 存储分配结果 (可能包含 dormId 或 message)
const loadingDorm = ref(true);   // 宿舍信息加载状态
const dormError = ref(null);     // 获取宿舍信息错误

const getUserId = () => {
    const userId = localStorage.getItem('user-id');
    if (!userId) {
        console.error("无法获取用户ID，请先登录！");
        fetchError.value = "无法获取用户身份，请重新登录。"; // 设置获取错误
        return null;
    }
    return parseInt(userId, 10);
};

// --- API Functions ---
// 获取个人信息
const fetchUserProfile = async () => {
    const userId = getUserId();
    if (!userId) {
        loading.value = false;
        return; // 如果没有 userId，停止执行
    }

    loading.value = true;
    fetchError.value = null; // 重置错误
    try {
        const response = await apiClient.get(`/users/${userId}/profile`);
        userInfo.value = response.data;
        // 初始化编辑表单数据
        resetEditableInfo();
    } catch (err) {
        console.error('获取用户信息失败:', err);
        if (err.response && err.response.status === 404) {
            fetchError.value = '未找到您的用户信息记录。';
        } else {
            fetchError.value = '加载用户信息失败，请稍后重试。';
        }
        userInfo.value = null; // 获取失败则清空
    } finally {
        loading.value = false;
    }
};

// 保存个人信息更新
const saveProfile = async () => {
    const userId = getUserId();
    if (!userId) return;

    isSaving.value = true;
    updateError.value = null;
    successMessage.value = null;

    // 只提交允许更新的字段
    const updateData = {
        name: editableUserInfo.name,
        email: editableUserInfo.email
        // 添加其他需要更新的字段
    };

    try {
        const response = await apiClient.put(`/users/${userId}/profile`, updateData);
        userInfo.value = response.data; // 使用后端返回的最新数据更新显示
        resetEditableInfo(); // 用最新数据重置编辑区
        isEditing.value = false; // 退出编辑模式
        successMessage.value = '个人信息更新成功！';
        // 清除成功消息 (可选)
        setTimeout(() => { successMessage.value = null; }, 3000);
    } catch (err) {
        console.error('更新用户信息失败:', err);
        if (err.response && err.response.data && err.response.data.message) {
            // 尝试显示后端验证错误或业务错误
            updateError.value = `更新失败: ${err.response.data.message}`;
        } else if (err.response && err.response.status === 400) {
            updateError.value = '更新失败: 请检查输入的信息格式是否正确。';
        }
        else {
            updateError.value = '更新用户信息失败，请稍后重试。';
        }
    } finally {
        isSaving.value = false;
    }
};

const fetchDormAssignment = async () => {
    const userId = getUserId();
    if (!userId) return; // 如果没有 userId, 不执行

    loadingDorm.value = true;
    dormError.value = null;
    try {
        console.log(`PersonalInfo.vue: Fetching dorm assignment for user ${userId}...`);
        // 调用新的后端接口
        const response = await apiClient.get(`/users/${userId}/dorm-assignment`);
        dormAssignment.value = response.data; // 存储后端返回的数据 (可能包含 dormId 或 message)
        console.log("PersonalInfo.vue: Dorm assignment fetched:", dormAssignment.value);
    } catch (err) {
        console.error('获取宿舍分配信息失败:', err);
        if (err.response?.status === 403) {
            dormError.value = "无权查看宿舍信息。";
        } else {
            dormError.value = "无法加载宿舍分配信息。";
        }
        dormAssignment.value = null; // 出错时清空
    } finally {
        loadingDorm.value = false;
    }
}
// --- Utility Functions ---
// 重置编辑表单的数据为当前 userInfo 的数据
const resetEditableInfo = () => {
    if (userInfo.value) {
        editableUserInfo.name = userInfo.value.name || '';
        editableUserInfo.email = userInfo.value.email || '';
        // 同步其他可编辑字段
    }
};

// 切换编辑状态
const toggleEdit = (editing) => {
    isEditing.value = editing;
    updateError.value = null; // 清除之前的更新错误
    successMessage.value = null; // 清除成功消息
    if (!editing) {
        // 如果是取消编辑，恢复编辑框为原始数据
        resetEditableInfo();
    }
};


// --- Lifecycle Hook ---
onMounted(async () => {
    await fetchUserProfile(); // 获取个人基础信息
    // 只有在获取到用户信息后（能拿到userId），才去获取宿舍信息
    if (userInfo.value) {
        await fetchDormAssignment(); // <--- 确认这行被调用了
    } else {
        // 如果连用户信息都没获取到，也就不需要获取宿舍信息了
        loadingDorm.value = false; // 结束宿舍加载状态
        dormError.value = "无法获取用户信息，因此无法查询宿舍分配。";
    }
});
</script>

<style scoped>
/* 复用或定义通用页面样式 */
@import './MbtiForm.vue?scoped=true';
/* 假设包含 page-container 等 */

.profile-details {
    background-color: #fff;
    padding: 25px;
    border-radius: 8px;
    border: 1px solid #eee;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.profile-item {
    display: flex;
    align-items: center;
    margin-bottom: 18px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;
}

.profile-item:last-of-type {
    border-bottom: none;
    margin-bottom: 25px;
    /* 最后一项与按钮间距 */
}


.profile-item label {
    min-width: 100px;
    /* 标签宽度对齐 */
    font-weight: bold;
    color: #555;
    margin-right: 15px;
}

.profile-item span {
    color: #333;
    word-break: break-all;
    /* 防止长邮箱或用户名溢出 */
}

/* 未填写时的样式 */
.profile-item span:empty::after {
    content: '未填写';
    color: #999;
    font-style: italic;
}

.profile-item.editable input[type="text"],
.profile-item.editable input[type="email"] {
    padding: 8px 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    flex-grow: 1;
    /* 输入框占据剩余空间 */
    font-size: 1em;
    max-width: 400px;
    /* 限制最大宽度 */
}

.profile-actions {
    display: flex;
    gap: 10px;
    /* 按钮间距 */
    margin-top: 20px;
    justify-content: flex-start;
    /* 按钮左对齐 */
}

.profile-actions button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1em;
    transition: background-color 0.3s, opacity 0.3s;
}

.edit-button {
    background-color: #007bff;
    color: white;
}

.edit-button:hover {
    background-color: #0056b3;
}

.save-button {
    background-color: #28a745;
    color: white;
}

.save-button:hover:not(:disabled) {
    background-color: #218838;
}

.cancel-button {
    background-color: #6c757d;
    color: white;
}

.cancel-button:hover:not(:disabled) {
    background-color: #5a6268;
}

.profile-actions button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
    opacity: 0.7;
}

.update-feedback {
    margin-top: 20px;
    padding: 10px 15px;
    border-radius: 4px;
    text-align: center;
}

.error-message.update-feedback {
    color: #721c24;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
}

.success-message.update-feedback {
    color: #155724;
    background-color: #d4edda;
    border: 1px solid #c3e6cb;
}

/* 加载和错误消息 */
.loading-message,
.error-message,
.no-data-message {
    text-align: center;
    padding: 30px;
    color: #666;
}

.error-message {
    color: #dc3545;
}
</style>