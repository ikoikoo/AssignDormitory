import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import MbtiForm from '../views/MbtiForm.vue'
import MbtiResult from '../views/MbtiResult.vue'
import DormHabitsForm from '../views/DormHabitsForm.vue'
import DormHabitsResult from '../views/DormHabitsResult.vue'
import Announcements from '../views/Announcements.vue'
import PersonalInfo from '../views/PersonalInfo.vue'
import DormManagement from '../views/admin/DormManagement.vue'
import { isAdmin, isAuthenticated } from '../utils/auth'

const AdminAnnouncements = () => import('../views/admin/AdminAnnouncements.vue') // 路由懒加载
const AdminDormInfo = () => import('../views/admin/AdminDormInfo.vue')
const getUserInfo = () => {
  const userInfoString = localStorage.getItem('user-info')
  if (!userInfoString) return null
  try {
    return JSON.parse(userInfoString)
  } catch (e) {
    return null
  }
}

const isAdminUser = () => {
  const userInfo = getUserInfo()
  return userInfo && userInfo.roles && userInfo.roles.includes('ROLE_ADMIN')
}
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }, // 添加页面标题元信息
  },
  {
    // <--- 添加注册路由
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册' },
  },
  {
    path: '/',
    // 如果用户已登录，直接去仪表盘，否则去登录页
    redirect: () => {
      return isAuthenticated() ? '/dashboard' : '/login'
    },
  },
  {
    // 仪表盘或个人主页，作为其他页面的容器或入口点
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, title: '个人主页' }, // 标记需要登录才能访问
    // 可以考虑将其他页面作为子路由，但这取决于你的布局设计
    // 这里暂时将它们作为顶级路由处理
  },
  {
    path: '/admin/dorms', // 管理员页面的路径
    name: 'AdminDormManagement',
    component: DormManagement,
    meta: { requiresAuth: true, requiresAdmin: true, title: '宿舍管理' },
  },
  {
    path: '/mbti',
    name: 'MbtiForm',
    component: MbtiForm,
    meta: { requiresAuth: true, title: 'MBTI问卷' },
  },
  {
    path: '/mbti-result/:result', // 使用动态路由参数 :result
    name: 'MbtiResult', // 定义结果页面的路由名称
    component: MbtiResult,
    // props: true, // 可以选择使用 props 接收参数，或者在组件内用 useRoute
    meta: { requiresAuth: true, title: 'MBTI 结果' },
  },
  {
    path: '/dorm-habits',
    name: 'DormHabitsForm',
    component: DormHabitsForm,
    meta: { requiresAuth: true, title: '宿舍习惯问卷' },
  },
  {
    path: '/dorm-habits-result', // 结果页面路径
    name: 'DormHabitsResult',
    component: DormHabitsResult,
    meta: { requiresAuth: true, title: '习惯评估结果' },
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: Announcements,
    meta: { requiresAuth: true, title: '公告' },
  },
  {
    path: '/profile',
    name: 'PersonalInfo',
    component: PersonalInfo,
    meta: { requiresAuth: true, title: '个人信息' },
  },
  // --- 管理员专属路由 ---
  {
    path: '/admin/announcements', // 公告管理页面
    name: 'AdminAnnouncements',
    component: AdminAnnouncements,
    meta: { requiresAuth: true, requiresAdmin: true, title: '公告管理' }, // 需要登录且是管理员
  },
  {
    // 路径与后端 Controller 对应 (@RequestMapping + @GetMapping)
    path: '/admin/dorm-info/all', // 查看所有宿舍信息页面
    name: 'AdminDormInfo',
    component: AdminDormInfo,
    meta: { requiresAuth: true, requiresAdmin: true, title: '宿舍信息查看' }, // 需要登录且是管理员
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 使用 HTML5 History 模式
  routes,
})

// --- ---
// function isAuthenticated() {
//   // 检查 localStorage 中是否有 token
//   return !!localStorage.getItem('user-token')
// }

router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 学生系统`
  } else {
    document.title = '学生系统'
  }
  const userInfo = getUserInfo() // 获取用户信息
  const isAdmin = userInfo && userInfo.roles && userInfo.roles.includes('ROLE_ADMIN')

  if (to.meta.requiresAdmin && !isAdminUser) {
    console.log(`导航守卫：需要管理员权限访问 ${to.path}，但用户不是管理员。重定向到仪表盘。`)
    next({ name: 'Dashboard' }) // 或者跳转到 403 页面
  } else {
    next() // 其他情况继续
    // 检查路由是否需要认证
    if (to.meta.requiresAuth && !isAuthenticated()) {
      // 需要登录但未登录 -> 跳转登录页
      console.log('守卫：需要登录 -> /login')
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else if (to.meta.requiresAdmin && !isAdmin) {
      // 需要管理员权限但用户不是管理员 -> 跳转到仪表盘 (或无权限页面)
      console.log('守卫：需要管理员 -> /dashboard')
      next({ name: 'Dashboard' }) // 或者 next({ name: 'Unauthorized' });
    } else if (to.name === 'Login' && isAuthenticated()) {
      // 已登录访问登录页 -> 跳转到仪表盘
      console.log('守卫：已登录访问 /login -> /dashboard')
      next({ name: 'Dashboard' })
    } else {
      // 其他情况，允许访问
      console.log('守卫：允许访问 ->', to.name || to.path)
      next()
    }
  }
})

export default router
