import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证token等
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('auth_token')
          break
        case 403:
          console.error('权限不足')
          break
        case 404:
          console.error('资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error('请求失败:', data?.detail || error.message)
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
    }
    
    return Promise.reject(error)
  }
)

// API接口定义
export const taskApi = {
  // 获取任务列表
  getTasks: (params = {}) => api.get('/tasks', { params }),
  
  // 获取单个任务
  getTask: (taskId) => api.get(`/tasks/${taskId}`),
  
  // 创建任务
  createTask: (data) => api.post('/tasks', data),
  
  // 更新任务
  updateTask: (taskId, data) => api.put(`/tasks/${taskId}`, data),
  
  // 删除任务
  deleteTask: (taskId) => api.delete(`/tasks/${taskId}`),
  
  // 取消任务
  cancelTask: (taskId) => api.post(`/tasks/${taskId}/cancel`),
  
  // 获取任务状态
  getTaskStatus: (taskId) => api.get(`/tasks/${taskId}/status`),
}

export const resultApi = {
  // 获取任务结果
  getTaskResults: (taskId) => api.get(`/results/task/${taskId}`),
  
  // 获取单个结果
  getResult: (resultId) => api.get(`/results/${resultId}`),
  
  // 删除结果
  deleteResult: (resultId) => api.delete(`/results/${resultId}`),
}

export const userApi = {
  // 获取用户列表
  getUsers: (params = {}) => api.get('/users', { params }),
  
  // 获取单个用户
  getUser: (userId) => api.get(`/users/${userId}`),
  
  // 创建用户
  createUser: (data) => api.post('/users', data),
  
  // 更新用户
  updateUser: (userId, data) => api.put(`/users/${userId}`, data),
  
  // 删除用户
  deleteUser: (userId) => api.delete(`/users/${userId}`),
}

// WebSocket连接管理
export class WebSocketManager {
  constructor() {
    this.connections = new Map()
  }

  connect(taskId, onMessage, onError, onClose) {
    const wsUrl = `ws://localhost:8000/ws/${taskId}`
    const ws = new WebSocket(wsUrl)
    
    ws.onopen = () => {
      console.log(`WebSocket连接已建立: ${taskId}`)
    }
    
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        onMessage(data)
      } catch (error) {
        console.error('WebSocket消息解析失败:', error)
      }
    }
    
    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      onError(error)
    }
    
    ws.onclose = () => {
      console.log(`WebSocket连接已关闭: ${taskId}`)
      this.connections.delete(taskId)
      onClose()
    }
    
    this.connections.set(taskId, ws)
    return ws
  }

  disconnect(taskId) {
    const ws = this.connections.get(taskId)
    if (ws) {
      ws.close()
      this.connections.delete(taskId)
    }
  }

  disconnectAll() {
    this.connections.forEach((ws, taskId) => {
      ws.close()
    })
    this.connections.clear()
  }
}

export default api
