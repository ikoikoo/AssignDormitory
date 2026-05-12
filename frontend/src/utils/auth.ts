import { type Router } from 'vue-router' // 导入 Router 类型

// 获取存储的用户信息
interface UserInfo {
  // 定义用户信息接口
  id: number
  username: string
  email: string
  roles: string[]
  // 添加其他可能的字段
}

export function getUserInfo(): UserInfo | null {
  // 指定返回值类型
  const info = localStorage.getItem('user-info')
  try {
    // 使用类型断言，如果我们确定解析结果是 UserInfo
    return info ? (JSON.parse(info) as UserInfo) : null
  } catch (e) {
    console.error('解析用户信息失败:', e)
    return null
  }
}

// 检查当前用户是否是管理员
export function isAdmin(): boolean {
  // 指定返回值类型 boolean
  const userInfo = getUserInfo()
  return !!(userInfo && Array.isArray(userInfo.roles) && userInfo.roles.includes('ROLE_ADMIN'))
}

// 检查是否已认证 (登录)
export function isAuthenticated(): boolean {
  // 指定返回值类型 boolean
  return !!localStorage.getItem('user-token')
}

// 退出登录函数
export function logoutUser(router?: Router): void {
  // 参数可选，无返回值 void
  console.log('执行退出登录 (工具函数)')
  localStorage.removeItem('user-token')
  localStorage.removeItem('user-id')
  localStorage.removeItem('user-info')
  if (router) {
    router.replace({ name: 'Login' })
  } else {
    window.location.href = '/login'
  }
}
