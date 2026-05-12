<template>
    <div class="page-container">
        <header class="page-header">

        </header>

        <div class="content-area">
            <!-- 1. 发布新公告按钮 -->
            <button @click="openAddModal" class="add-button">
                <i class="fas fa-plus"></i> 发布新公告
            </button>

            <!-- 2. 公告列表显示区域 -->
            <div v-if="loading" class="loading-message">正在加载公告列表...</div>
            <div v-else-if="loadError" class="error-message">加载失败: {{ loadError }}</div>
            <div v-else-if="announcements.length === 0" class="no-data-message">暂无公告。</div>

            <!-- 3. 公告表格 -->
            <table v-else class="announcements-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>标题</th>
                        <th>内容摘要</th>
                        <th>发布时间</th>
                        <th style="width: 150px;">操作</th> <!-- 调整宽度 -->
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="announcement in announcements" :key="announcement.id">
                        <td>{{ announcement.id }}</td>
                        <td>{{ announcement.title }}</td>
                        <td>{{ truncateContent(announcement.content) }}</td>
                        <td>{{ formatDateTime(announcement.publishDate) }}</td>
                        <td>
                            <!-- 编辑和删除按钮 -->
                            <button @click="openEditModal(announcement)" class="action-button edit" title="编辑">
                                <i class="fas fa-edit"></i> 编辑
                            </button>
                            <button @click="confirmDelete(announcement)" class="action-button delete" title="删除">
                                <i class="fas fa-trash-alt"></i> 删除
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- 4. 新增/编辑公告的模态框 -->
        <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
            <div class="modal-content">
                <h2>{{ isEditing ? '编辑公告' : '发布新公告' }}</h2>
                <form @submit.prevent="saveAnnouncement">
                    <div class="form-group">
                        <label for="ann-title">标题:</label>
                        <!-- required 属性提供基础浏览器验证 -->
                        <input type="text" id="ann-title" v-model="currentAnnouncement.title" required maxlength="200">
                    </div>
                    <div class="form-group">
                        <label for="ann-content">内容:</label>
                        <textarea id="ann-content" v-model="currentAnnouncement.content" rows="8" required></textarea>
                    </div>
                    <!-- 显示模态框内的错误 -->
                    <div v-if="modalError" class="error-message modal-error">{{ modalError }}</div>
                    <div class="modal-actions">
                        <!-- 提交按钮，处理中禁用 -->
                        <button type="submit" :disabled="isSaving" class="save-button">
                            <i v-if="isSaving" class="fas fa-spinner fa-spin"></i>
                            {{ isSaving ? ' 保存中...' : '保存' }}
                        </button>
                        <!-- 取消按钮 -->
                        <button type="button" @click="closeModal" class="cancel-button" :disabled="isSaving">取消</button>
                    </div>
                </form>
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import apiClient from '@/api/axios';
import { format } from 'date-fns';
import { isAdmin } from '@/utils/auth';

// --- 状态管理 (State) ---
const announcements = ref([]); // 公告列表
const loading = ref(true);     // 列表加载状态
const loadError = ref(null);   // 列表加载错误
const showModal = ref(false);  // 是否显示模态框
const isEditing = ref(false);  // 模态框是用于编辑还是新增
const currentAnnouncement = reactive({ // 当前正在编辑或新增的公告数据
    id: null,
    title: '',
    content: ''
});
const isSaving = ref(false);   // 是否正在保存 (提交中)
const modalError = ref(null);  // 模态框内的错误信息

// --- API 调用函数 ---
const isUserAdmin = computed(() => isAdmin());
console.log('管理员状态 (setup):', isAdmin()); // 直接打印
onMounted(() => {
    console.log('管理员状态 (mounted):', isAdmin()); // 挂载时再打印一次
});

// --- (调试用) 显示用户角色 (这里使用了 computed) ---
const userRoles = computed(() => { // <--- computed 在这里使用
    const userInfo = getUserInfo();
    return userInfo?.roles?.join(', ') || '未登录或无角色';
});

// 获取公告列表
const fetchAnnouncements = async () => {
    loading.value = true;
    loadError.value = null;
    console.log("尝试获取公告列表...");
    try {
        const response = await apiClient.get('/announcements'); // GET /api/announcements
        announcements.value = response.data || [];
        console.log("公告列表获取成功:", announcements.value);
    } catch (err) {
        console.error('获取公告列表失败:', err);
        loadError.value = err.response?.data?.message || err.message || '无法加载公告数据';
    } finally {
        loading.value = false;
    }
};

// 保存公告 (处理新增 POST 和 更新 PUT)
const saveAnnouncement = async () => {
    // 基础前端验证
    if (!currentAnnouncement.title?.trim() || !currentAnnouncement.content?.trim()) {
        modalError.value = "公告标题和内容都不能为空。";
        return;
    }

    isSaving.value = true;
    modalError.value = null;
    const announcementData = { // 只发送需要保存的字段
        title: currentAnnouncement.title.trim(),
        content: currentAnnouncement.content.trim()
    };

    try {
        let response;
        if (isEditing.value && currentAnnouncement.id) {
            // --- 更新公告 ---
            console.log(`尝试更新公告 (ID: ${currentAnnouncement.id})`, announcementData);
            response = await apiClient.put(`/announcements/${currentAnnouncement.id}`, announcementData); // PUT /api/announcements/{id}
            console.log("公告更新成功:", response.data);
        } else {
            // --- 新增公告 ---
            console.log("尝试新增公告", announcementData);
            response = await apiClient.post('/announcements', announcementData); // POST /api/announcements
            console.log("公告新增成功:", response.data);
        }
        closeModal(); // 关闭模态框
        await fetchAnnouncements(); // 重新加载列表以显示更新
    } catch (err) {
        console.error('保存公告失败:', err);
        modalError.value = err.response?.data?.message || err.message || '操作失败，请重试';
    } finally {
        isSaving.value = false;
    }
};

// 删除公告
const deleteAnnouncement = async (id) => {
    console.log(`尝试删除公告 (ID: ${id})`);
    // 这里可以设置一个全局 loading 或特定行的 loading 状态
    try {
        await apiClient.delete(`/announcements/${id}`); // DELETE /api/announcements/{id}
        console.log(`公告 (ID: ${id}) 删除成功`);
        // 从前端列表移除，或重新获取列表
        announcements.value = announcements.value.filter(ann => ann.id !== id);
        // 或者 await fetchAnnouncements(); // 重新请求
    } catch (err) {
        console.error(`删除公告 (ID: ${id}) 失败:`, err);
        // 在页面主区域显示删除错误
        loadError.value = err.response?.data?.message || err.message || '删除失败';
        // 可选: 定时清除错误信息
        setTimeout(() => { loadError.value = null; }, 5000);
    }
};

// --- 辅助函数 ---

// 打开新增模态框
const openAddModal = () => {
    isEditing.value = false;
    Object.assign(currentAnnouncement, { id: null, title: '', content: '' }); // 重置表单
    modalError.value = null; // 清除模态框错误
    showModal.value = true;
};

// 打开编辑模态框，填充数据
const openEditModal = (announcement) => {
    isEditing.value = true;
    // 使用展开运算符创建副本，避免直接修改列表中的响应式对象
    Object.assign(currentAnnouncement, { ...announcement });
    modalError.value = null;
    showModal.value = true;
};

// 关闭模态框
const closeModal = () => {
    showModal.value = false;
};

// 确认删除对话框
const confirmDelete = (announcement) => {
    // 使用浏览器自带的确认框
    if (window.confirm(`您确定要删除公告 "${announcement.title}" (ID: ${announcement.id}) 吗？此操作无法撤销。`)) {
        deleteAnnouncement(announcement.id);
    }
};

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'N/A';
    try {
        // 使用 date-fns 格式化
        return format(new Date(dateTimeString), 'yyyy-MM-dd HH:mm:ss'); // 更详细的格式
    } catch (e) {
        console.error("日期格式化失败:", e);
        return dateTimeString; // 出错时返回原始字符串
    }
};

// 截断内容用于表格预览
const truncateContent = (content, maxLength = 30) => { // 摘要可以短一点
    if (!content) return '';
    const cleanedContent = content.replace(/<[^>]*>?/gm, ''); // 移除可能的 HTML 标签
    if (cleanedContent.length <= maxLength) {
        return cleanedContent;
    }
    return cleanedContent.substring(0, maxLength) + '...';
};

// --- 生命周期钩子 ---
onMounted(() => {
    fetchAnnouncements(); // 组件挂载时获取初始公告列表
});
</script>

<style scoped>
/* --- 基础页面和内容区域样式 (复用或定义) --- */
.page-container {
    padding: 20px;
    max-width: 1100px;
    margin: auto;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
}

.page-header h1 {
    margin: 0;
    font-size: 1.8em;
}

.back-link {
    text-decoration: none;
    color: #007bff;
    font-size: 0.95em;
}

.content-area {
    background-color: #fff;
    padding: 25px;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.08);
}

/* --- 按钮样式 --- */
.add-button {
    background-color: #198754;
    /* 更深的绿色 */
    color: white;
    padding: 10px 18px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    margin-bottom: 25px;
    font-size: 1.05em;
    transition: background-color 0.2s;
}

.add-button:hover {
    background-color: #146c43;
}

.add-button i {
    margin-right: 6px;
}

/* 图标间距 */

.action-button {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin: 0 4px;
    /* 按钮间距 */
    font-size: 0.9em;
    transition: opacity 0.2s;
}

.action-button:hover {
    opacity: 0.85;
}

.action-button i {
    margin-right: 4px;
}

/* 图标间距 */

.action-button.edit {
    background-color: #ffc107;
    color: #333;
}

.action-button.delete {
    background-color: #dc3545;
    color: white;
}

/* --- 表格样式 --- */
.announcements-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
    font-size: 0.95em;
}

.announcements-table th,
.announcements-table td {
    border: 1px solid #e0e0e0;
    padding: 12px 15px;
    text-align: left;
    vertical-align: middle;
    /* 垂直居中 */
}

.announcements-table th {
    background-color: #f8f9fa;
    font-weight: 600;
    /*稍粗*/
    color: #495057;
}

.announcements-table tbody tr:nth-child(odd) {
    background-color: #fdfdfd;
}

.announcements-table tbody tr:hover {
    background-color: #f1f1f1;
}

.announcements-table td:last-child {
    /* 操作列 */
    text-align: center;
    white-space: nowrap;
    /* 防止按钮换行 */
}

/* --- 模态框样式 (与之前类似，可微调) --- */
.modal-overlay {
    position: fixed;
    /* ... */
    z-index: 1000;
    /* ... */
}

.modal-content {
    background-color: white;
    padding: 30px 40px;
    /* ... */
}

.modal-content h2 {
    margin-top: 0;
    margin-bottom: 25px;
    text-align: center;
    color: #333;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    color: #495057;
}

.form-group input[type="text"],
.form-group textarea {
    width: 100%;
    padding: 12px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 1em;
}

.form-group textarea {
    resize: vertical;
    min-height: 120px;
}

.modal-actions {
    margin-top: 30px;
    text-align: right;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

/* 使用flex布局按钮 */
.modal-actions button {
    padding: 10px 20px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1em;
}

.modal-actions button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
    border-color: #ccc;
}

.modal-error {
    margin-top: 15px;
    text-align: left;
    padding: 8px;
    font-size: 0.9em;
}

/* 错误显示在表单内 */
.save-button i {
    margin-right: 5px;
}
</style>