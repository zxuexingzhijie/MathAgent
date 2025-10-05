# 前端集成适配指南

## 📋 当前前端状态分析

### 现有前端架构
```
frontend/
├── src/
│   ├── services/api.js          # API 服务层
│   ├── components/              # 组件
│   │   └── CreateTaskForm.vue   # 创建任务表单
│   ├── views/                   # 视图
│   │   ├── ChatView.vue         # 聊天视图
│   │   ├── TasksView.vue        # 任务列表
│   │   └── ...
│   └── stores/taskStore.js      # 状态管理
├── vite.config.js               # Vite 配置
└── package.json                 # 依赖配置
```

### 技术栈
- Vue 3 + Vite
- Element Plus (UI 组件库)
- Axios (HTTP 客户端)
- Pinia (状态管理)
- Markdown 渲染

---

## ⚠️ 需要修改的部分

### 1. **API 路径和接口适配** 🔴 必须修改

#### 问题
现有前端 API 路径与 Java 后端不匹配：

**前端当前 API** (`api.js`):
```javascript
// 任务相关
getTasks: () => api.get('/tasks')
createTask: (taskData) => api.post('/tasks', taskData)
getTaskDetail: (taskId) => api.get(`/tasks/${taskId}`)
executeTask: (taskId) => api.post(`/tasks/${taskId}/execute`)

// 聊天相关
getSessions: () => api.get('/chat/sessions')
createSession: (data) => api.post('/chat/sessions', data)
sendMessage: (sessionId, content) => ...
```

**Java 后端实际 API** (`ModelingController.java`):
```java
POST   /api/modeling/submit           // 提交建模任务
GET    /api/modeling/generate-task-id // 生成任务ID
GET    /api/modeling/health           // 健康检查
```

#### 解决方案

创建新的 `modelingApi.js` 文件：

```javascript
// src/services/modelingApi.js
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
   * @param {string} problemData.compTemplate - 竞赛模板 (CHINA/USA/GLOBAL)
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
```

---

### 2. **WebSocket 连接适配** 🟡 推荐修改

#### 问题
前端需要实时接收后端的进度更新。

#### 解决方案

创建 `websocket.js` 服务：

```javascript
// src/services/websocket.js
import { ref } from 'vue'

class WebSocketService {
  constructor() {
    this.ws = null
    this.connected = ref(false)
    this.messages = ref([])
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
  }

  /**
   * 连接 WebSocket
   * @param {string} taskId - 任务ID
   */
  connect(taskId) {
    const wsUrl = `ws://localhost:8080/api/ws/modeling/${taskId}`
    
    try {
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket 连接成功')
        this.connected.value = true
        this.reconnectAttempts = 0
      }
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('收到 WebSocket 消息:', message)
          this.messages.value.push(message)
          
          // 触发自定义事件
          this.onMessage(message)
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.onError(error)
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.connected.value = false
        this.attemptReconnect(taskId)
      }
      
    } catch (error) {
      console.error('创建 WebSocket 连接失败:', error)
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect(taskId) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect(taskId)
      }, this.reconnectDelay)
    } else {
      console.error('达到最大重连次数，停止重连')
    }
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket 未连接')
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 消息处理回调（由外部设置）
   */
  onMessage(message) {
    // 由外部组件覆盖此方法
  }

  /**
   * 错误处理回调（由外部设置）
   */
  onError(error) {
    // 由外部组件覆盖此方法
  }
}

export default new WebSocketService()
```

---

### 3. **CreateTaskForm 组件适配** 🟡 推荐修改

#### 修改后的组件：

```vue
<template>
  <div class="create-task-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="left"
    >
      <el-form-item label="问题描述" prop="quesAll">
        <el-input
          v-model="form.quesAll"
          type="textarea"
          :rows="8"
          placeholder="请详细描述您的数学建模问题..."
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="竞赛模板" prop="compTemplate">
        <el-select v-model="form.compTemplate" placeholder="请选择竞赛模板" style="width: 100%">
          <el-option label="中国赛" value="CHINA" />
          <el-option label="美国赛" value="USA" />
          <el-option label="国际赛" value="GLOBAL" />
          <el-option label="通用" value="GENERAL" />
        </el-select>
      </el-form-item>

      <el-form-item label="输出格式" prop="formatOutput">
        <el-radio-group v-model="form.formatOutput">
          <el-radio label="MARKDOWN">Markdown</el-radio>
          <el-radio label="LATEX">LaTeX</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            <el-icon><Promotion /></el-icon>
            开始建模
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { modelingApi } from '@/services/modelingApi'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

// 表单数据
const form = reactive({
  quesAll: '',
  compTemplate: 'CHINA',
  formatOutput: 'MARKDOWN'
})

// 表单验证规则
const rules = {
  quesAll: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 50, message: '问题描述至少50个字符', trigger: 'blur' }
  ],
  compTemplate: [
    { required: true, message: '请选择竞赛模板', trigger: 'change' }
  ],
  formatOutput: [
    { required: true, message: '请选择输出格式', trigger: 'change' }
  ]
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 生成任务ID
    const taskId = await modelingApi.generateTaskId()
    console.log('生成任务ID:', taskId)
    
    // 提交建模任务
    const result = await modelingApi.submitProblem({
      taskId: taskId,
      quesAll: form.quesAll,
      compTemplate: form.compTemplate,
      formatOutput: form.formatOutput
    })
    
    ElMessage.success(result.message || '任务提交成功！')
    
    // 跳转到任务详情页面（实时查看进度）
    router.push({
      name: 'TaskDetail',
      params: { taskId: taskId }
    })
    
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style lang="scss" scoped>
.create-task-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 24px;
  }
}
</style>
```

---

### 4. **TaskDetailView 组件（新建）** 🟢 新增

创建任务详情页面，显示实时进度：

```vue
<template>
  <div class="task-detail-view">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">任务详情: {{ taskId }}</span>
      </template>
    </el-page-header>

    <div class="task-content">
      <!-- 进度时间线 -->
      <el-card class="progress-card">
        <template #header>
          <div class="card-header">
            <span>任务进度</span>
            <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
          </div>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in steps"
            :key="index"
            :type="step.type"
            :icon="step.icon"
            :timestamp="step.timestamp"
          >
            <div class="step-content">
              <h4>{{ step.title }}</h4>
              <p>{{ step.description }}</p>
              <div v-if="step.loading" class="step-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>处理中...</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 消息日志 -->
      <el-card class="messages-card">
        <template #header>
          <span>实时日志</span>
        </template>

        <div class="messages-list">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message-item', message.level]"
          >
            <el-icon>
              <InfoFilled v-if="message.level === 'INFO'" />
              <SuccessFilled v-if="message.level === 'SUCCESS'" />
              <WarningFilled v-if="message.level === 'WARNING'" />
              <CircleCloseFilled v-if="message.level === 'ERROR'" />
            </el-icon>
            <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            <span class="message-text">{{ message.content }}</span>
          </div>
        </div>
      </el-card>

      <!-- 结果展示 -->
      <el-card v-if="completed" class="result-card">
        <template #header>
          <span>建模结果</span>
        </template>

        <div class="result-content">
          <el-button type="primary" @click="downloadResult">
            <el-icon><Download /></el-icon>
            下载完整论文
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import websocketService from '@/services/websocket'
import dayjs from 'dayjs'
import {
  InfoFilled,
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled,
  Loading,
  Download
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const taskId = ref(route.params.taskId)

const messages = ref([])
const completed = ref(false)
const currentStep = ref(0)

const steps = ref([
  {
    title: '问题分析',
    description: '正在分析和分解问题...',
    type: 'primary',
    icon: null,
    timestamp: '',
    loading: true
  },
  {
    title: '数学建模',
    description: '等待中...',
    type: 'info',
    icon: null,
    timestamp: '',
    loading: false
  },
  {
    title: '代码编写',
    description: '等待中...',
    type: 'info',
    icon: null,
    timestamp: '',
    loading: false
  },
  {
    title: '论文撰写',
    description: '等待中...',
    type: 'info',
    icon: null,
    timestamp: '',
    loading: false
  }
])

const statusType = computed(() => {
  if (completed.value) return 'success'
  if (currentStep.value === -1) return 'danger'
  return 'primary'
})

const statusText = computed(() => {
  if (completed.value) return '已完成'
  if (currentStep.value === -1) return '失败'
  return '进行中'
})

// WebSocket 消息处理
const handleWebSocketMessage = (message) => {
  console.log('处理 WebSocket 消息:', message)
  
  // 添加到消息列表
  messages.value.push({
    level: message.level || 'INFO',
    content: message.content || message.message,
    timestamp: message.timestamp || new Date().toISOString()
  })

  // 更新步骤状态
  if (message.content) {
    if (message.content.includes('识别用户意图')) {
      updateStep(0, 'SUCCESS', message.content)
    } else if (message.content.includes('建模')) {
      updateStep(1, 'SUCCESS', message.content)
    } else if (message.content.includes('代码')) {
      updateStep(2, 'SUCCESS', message.content)
    } else if (message.content.includes('论文')) {
      updateStep(3, 'SUCCESS', message.content)
    }
  }

  // 检查是否完成
  if (message.level === 'SUCCESS' && message.content?.includes('完成')) {
    completed.value = true
    steps.value.forEach(step => step.loading = false)
  }
}

// 更新步骤状态
const updateStep = (index, type, description) => {
  if (index < steps.value.length) {
    steps.value[index].type = type.toLowerCase()
    steps.value[index].description = description
    steps.value[index].timestamp = dayjs().format('HH:mm:ss')
    steps.value[index].loading = false
    
    // 开始下一步
    if (index + 1 < steps.value.length) {
      steps.value[index + 1].loading = true
    }
    
    currentStep.value = index + 1
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss')
}

// 下载结果
const downloadResult = () => {
  // TODO: 实现下载逻辑
  window.open(`/api/modeling/tasks/${taskId.value}/result`, '_blank')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  // 连接 WebSocket
  websocketService.connect(taskId.value)
  websocketService.onMessage = handleWebSocketMessage
  websocketService.onError = (error) => {
    console.error('WebSocket 错误:', error)
    messages.value.push({
      level: 'ERROR',
      content: '连接断开，请刷新页面重试',
      timestamp: new Date().toISOString()
    })
  }
})

onUnmounted(() => {
  // 断开 WebSocket
  websocketService.disconnect()
})
</script>

<style lang="scss" scoped>
.task-detail-view {
  padding: 24px;

  .page-title {
    font-size: 18px;
    font-weight: 500;
  }

  .task-content {
    margin-top: 24px;
    display: grid;
    gap: 24px;

    .progress-card,
    .messages-card,
    .result-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .step-content {
      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
      }

      p {
        margin: 0;
        color: #666;
      }

      .step-loading {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 8px;
        color: #409eff;
      }
    }

    .messages-list {
      max-height: 400px;
      overflow-y: auto;

      .message-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-bottom: 1px solid #f0f0f0;

        &.INFO {
          color: #409eff;
        }

        &.SUCCESS {
          color: #67c23a;
        }

        &.WARNING {
          color: #e6a23c;
        }

        &.ERROR {
          color: #f56c6c;
        }

        .message-time {
          color: #999;
          font-size: 12px;
          min-width: 70px;
        }

        .message-text {
          flex: 1;
        }
      }
    }

    .result-content {
      text-align: center;
      padding: 24px;
    }
  }
}
</style>
```

---

### 5. **路由配置更新** 🟡 推荐修改

更新 `router/index.js`:

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import CreateTaskView from '@/views/CreateTaskView.vue'
import TaskDetailView from '@/views/TaskDetailView.vue'
import ChatView from '@/views/ChatView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/create',
    name: 'CreateTask',
    component: CreateTaskView
  },
  {
    path: '/task/:taskId',
    name: 'TaskDetail',
    component: TaskDetailView
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatView
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
```

---

### 6. **环境变量配置** 🟢 建议修改

更新 `.env` 文件：

```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# WebSocket URL
VITE_WS_BASE_URL=ws://localhost:8080/api/ws

# 应用配置
VITE_APP_TITLE=数学建模Agent - Java版
VITE_APP_VERSION=1.0.0

# 功能开关
VITE_ENABLE_MOCK=false
```

---

## 📦 需要安装的新依赖

```bash
# 已有依赖，无需额外安装
# vue, vue-router, pinia, axios, element-plus
# dayjs, markdown-it
```

---

## 🔄 完整修改清单

### 必须修改 🔴
1. ✅ 创建 `modelingApi.js` - 适配 Java 后端 API
2. ✅ 创建 `websocket.js` - WebSocket 服务
3. ✅ 修改 `CreateTaskForm.vue` - 表单字段适配

### 推荐修改 🟡
4. ✅ 创建 `TaskDetailView.vue` - 实时进度显示
5. ✅ 更新路由配置
6. ✅ 更新环境变量配置

### 可选修改 🟢
7. 移除未使用的旧 API（`api.js` 中的 task 和 chat API）
8. 添加管理界面（提示词管理）
9. 优化错误处理和用户提示

---

## 🚀 部署和测试流程

### 1. 安装依赖
```bash
cd frontend
npm install
# 或
pnpm install
```

### 2. 创建新文件
```bash
# 创建新的 API 服务
touch src/services/modelingApi.js
touch src/services/websocket.js

# 创建新视图
touch src/views/TaskDetailView.vue
```

### 3. 启动开发服务器
```bash
# 先启动 Java 后端
cd ../math-model-agent-java
mvn spring-boot:run

# 然后启动前端
cd ../frontend
npm run dev
```

### 4. 测试流程
1. 访问 http://localhost:3000
2. 进入"创建任务"页面
3. 输入问题描述
4. 提交任务
5. 自动跳转到任务详情页
6. 查看实时进度和日志
7. 任务完成后下载结果

---

## 📊 API 对照表

| 功能 | 前端调用 | Java 后端端点 | 请求方法 |
|------|---------|--------------|---------|
| 生成任务ID | `modelingApi.generateTaskId()` | `/api/modeling/generate-task-id` | GET |
| 提交任务 | `modelingApi.submitProblem(data)` | `/api/modeling/submit` | POST |
| 健康检查 | `modelingApi.healthCheck()` | `/api/modeling/health` | GET |
| WebSocket连接 | `websocketService.connect(taskId)` | `/api/ws/modeling/{taskId}` | WebSocket |
| 列出提示词 | `promptApi.listPrompts()` | `/api/admin/prompts/list` | GET |
| 重载提示词 | `promptApi.reloadPrompt(fileName)` | `/api/admin/prompts/reload/{fileName}` | POST |

---

## 🔧 注意事项

### 1. CORS 配置
确保 Java 后端的 `CorsConfig.java` 已配置正确：
```java
cors:
  allowed-origins: http://localhost:3000,http://localhost:5173
```

### 2. WebSocket 配置
确保 `WebSocketConfig.java` 的端点路径匹配：
```java
registry.addEndpoint("/ws/modeling/{taskId}")
```

### 3. 超时时间
建模任务可能需要较长时间，建议：
- 前端 Axios 超时设置为 5 分钟
- 后端设置合理的超时时间
- WebSocket 保持连接

### 4. 错误处理
- 统一使用 `ApiResponse` 格式
- 前端拦截器处理响应格式
- 提供友好的错误提示

---

## ✅ 总结

**最小修改方案**（快速上线）：
1. 创建 `modelingApi.js`
2. 修改 `CreateTaskForm.vue`
3. 创建简单的任务详情页

**完整方案**（推荐）：
1. 创建所有新文件
2. 实现完整的 WebSocket 实时通信
3. 添加详细的进度展示
4. 实现结果下载功能

**估计工作量**：
- 最小方案: 2-3 小时
- 完整方案: 1-2 天

前端适配相对简单，主要是调整 API 调用和添加实时进度显示。核心工作在于 WebSocket 连接和消息处理。
