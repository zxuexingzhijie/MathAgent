import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证token等
    console.log('发送请求:', config.method, config.url, config.data)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response.status, response.data)
    return response
  },
  (error) => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 任务相关API
export const taskApi = {
  // 获取任务列表
  getTasks: () => api.get('/tasks'),
  
  // 创建任务
  createTask: (taskData) => api.post('/tasks', taskData),
  
  // 获取任务详情
  getTaskDetail: (taskId) => api.get(`/tasks/${taskId}`),
  
  // 执行任务
  executeTask: (taskId) => api.post(`/tasks/${taskId}/execute`),
  
  // 取消任务
  cancelTask: (taskId) => api.post(`/tasks/${taskId}/cancel`),
  
  // 获取任务结果
  getTaskResults: (taskId) => api.get(`/tasks/${taskId}/results`),
  
  // 获取任务日志
  getTaskLogs: (taskId) => api.get(`/tasks/${taskId}/logs`)
}

// 统计相关API - 后端未实现，暂时移除
// export const statsApi = {
//   // 获取系统统计信息
//   getStatistics: () => api.get('/statistics')
// }

export default api