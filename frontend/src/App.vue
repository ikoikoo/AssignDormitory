<template>
  <nav v-if="isUserLoggedIn"> <!-- 只在登录后显示导航？ -->
    <span v-if="isUserAdmin"> <!-- v-if 判断是否是管理员 -->
    </span>
  </nav>
  <router-view />
</template>

<script setup>
import { computed, watch } from 'vue'; // 引入 watch (如果需要响应路由变化)
import { useRoute, useRouter } from 'vue-router';
import { isAdmin, logoutUser, isAuthenticated } from '@/utils/auth'; // 导入工具函数

const router = useRouter();
const route = useRoute(); // 获取当前路由信息

// 使用计算属性来响应式地检查状态
const isUserAdmin = computed(() => isAdmin());
const isUserLoggedIn = computed(() => isAuthenticated());

const handleLogout = () => {
  logoutUser(router); // 调用退出登录工具函数
};
</script>

<style>
/* 基本的页面过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 可以在这里或 main.css 添加全局容器样式 */
#app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>