<template>
    <div class="page-container">
      <header class="page-header">
        <h1>个人信息</h1>
         <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
      </header>
      <div class="content-area">
        <p>管理你的个人信息。</p>
        <!-- 显示和编辑个人信息的表单 -->
        <form @submit.prevent="updateProfile">
          <div>
            <label>姓名:</label>
            <span>{{ userInfo.name }}</span>
          </div>
           <div>
            <label>学号:</label>
            <span>{{ userInfo.username }}</span>
          </div>
           <div>
            <label>邮箱:</label>
            <input type="email" v-model="editableUserInfo.email">
          </div>
          <!-- 更多字段 -->
          <button type="submit" class="submit-button">更新信息</button>
        </form>
      </div>
    </div>
  </template>
  <script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const userInfo = ref({}); // 用于显示
  const editableUserInfo = reactive({ email: '' }); // 用于编辑
  
  onMounted(() => {
    // 从 localStorage 或 API 获取当前用户信息
    const storedInfo = localStorage.getItem('user-info');
     if (storedInfo) {
       try {
         userInfo.value = JSON.parse(storedInfo);
         editableUserInfo.email = userInfo.value.email || ''; // 假设有 email 字段
       } catch(e) { console.error(e);}
     }
  });
  
  const updateProfile = () => {
     console.log('更新个人信息:', editableUserInfo);
     // 调用 API 更新用户信息
     alert('个人信息已更新（模拟）');
     // 可能需要更新 localStorage
     // router.push('/dashboard'); // 可以选择不跳转
  }
  </script>
  <style scoped>
     @import './MbtiForm.vue?scoped=true';
     .content-area form div { margin-bottom: 15px; }
     .content-area form label { display: inline-block; width: 80px; font-weight: bold; margin-right: 10px; }
     .content-area form span { color: #555; }
     .content-area form input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; min-width: 250px;}
  </style>