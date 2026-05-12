<template>
    <div class="page-container">
      <header class="page-header">
        <h1>宿舍习惯问卷</h1>
        <router-link to="/dashboard" class="back-link">返回个人主页</router-link>
      </header>
      <div class="content-area">
        <p>请认真填写你的宿舍生活习惯。</p>
        <!-- 添加宿舍习惯问卷表单 -->
        <form @submit.prevent="submitHabits">
           <!-- 问卷问题 -->
          <button type="submit" class="submit-button">提交习惯</button>
        </form>
      </div>
    </div>
  </template>
  <script setup>
  import { useRouter } from 'vue-router';
  const router = useRouter();
  const submitHabits = () => {
    console.log('提交宿舍习惯...');
    alert('宿舍习惯已提交（模拟）');
    router.push('/dashboard');
  };
  </script>
  <style scoped>
    /* 复用 MbtiForm.vue 的样式或全局样式 */
    @import './MbtiForm.vue?scoped=true'; /* Vite 支持导入其他 SFC 的 scoped CSS */
     /* 或者定义独立样式 */
  </style>