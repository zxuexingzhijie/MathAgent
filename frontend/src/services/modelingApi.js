import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 300000, // 5分钟超时（建模任务可能较长）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config.method, config.url, config.data)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 适配 Java ApiResponse 格式
api.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response.status, response.data)
    
    // Java 后端返回格式: { success: true, message: "...", data: {...} }
    if (response.data && typeof response.data.success !== 'undefined') {
      if (response.data.success) {
        return response.data
      } else {
        return Promise.reject(new Error(response.data.message || '请求失败'))
      }
    }
    
    return response.data
  },
  (error) => {
    console.error('API请求错误:', error)
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

// 数学建模 API
export const modelingApi = {
  /**
   * 生成新的任务ID
   * @returns {Promise<string>} 任务ID
   */
  generateTaskId: async () => {
    const response = await api.get('/modeling/generate-task-id')
    return response.data // 直接返回任务ID字符串
  },

  /**
   * 提交建模问题
   * @param {Object} problemData 问题数据
   * @param {string} problemData.taskId - 任务ID
   * @param {string} problemData.quesAll - 问题描述
   * @param {string} problemData.compTemplate - 竞赛模板 (CHINA/USA/GLOBAL/GENERAL)
   * @param {string} problemData.formatOutput - 输出格式 (MARKDOWN/LATEX)
   * @returns {Promise<Object>} 提交结果
   */
  submitProblem: async (problemData) => {
    const payload = {
      task_id: problemData.taskId,
      ques_all: problemData.quesAll,
      comp_template: problemData.compTemplate || 'CHINA',
      format_output: problemData.formatOutput || 'MARKDOWN'
    }
    
    const response = await api.post('/modeling/submit', payload)
    return response
  },

  /**
   * 健康检查
   * @returns {Promise<Object>}
   */
  healthCheck: async () => {
    const response = await api.get('/modeling/health')
    return response
  }
}

// 提示词管理 API
export const promptApi = {
  /**
   * 获取所有提示词列表
   */
  listPrompts: async () => {
    const response = await api.get('/admin/prompts/list')
    return response.data
  },

  /**
   * 获取特定提示词内容
   * @param {string} fileName - 文件名 (coordinator/modeler/coder/writer)
   */
  getPrompt: async (fileName) => {
    const response = await api.get(`/admin/prompts/${fileName}`)
    return response.data
  },

  /**
   * 重新加载提示词
   * @param {string} fileName - 文件名
   */
  reloadPrompt: async (fileName) => {
    const response = await api.post(`/admin/prompts/reload/${fileName}`)
    return response
  },

  /**
   * 重新加载所有提示词
   */
  reloadAllPrompts: async () => {
    const response = await api.post('/admin/prompts/reload-all')
    return response
  },

  /**
   * 检查提示词是否已加载
   * @param {string} fileName - 文件名
   */
  checkPrompt: async (fileName) => {
    const response = await api.get(`/admin/prompts/check/${fileName}`)
    return response.data
  },

  /**
   * 获取提示词统计信息
   */
  getStats: async () => {
    const response = await api.get('/admin/prompts/stats')
    return response.data
  }
}

export default api
