<template>
  <div class="task-detail-view">
    <!-- 任务基本信息 -->
    <el-card class="task-info-card" v-if="task">
      <template #header>
        <div class="card-header">
          <h2>{{ task.title }}</h2>
          <div class="task-meta">
            <el-tag :type="getStatusTag(task.status)">
              {{ getStatusLabel(task.status) }}
            </el-tag>
            <el-tag :type="getTaskTypeTag(task.task_type)">
              {{ getTaskTypeLabel(task.task_type) }}
            </el-tag>
            <span class="create-time">
              创建时间: {{ formatTime(task.created_at) }}
            </span>
          </div>
        </div>
      </template>

      <div class="task-content">
        <div class="task-description">
          <h3>问题描述</h3>
          <p>{{ task.description }}</p>
        </div>

        <div class="task-constraints" v-if="task.constraints">
          <h3>约束条件</h3>
          <p>{{ task.constraints }}</p>
        </div>

        <div class="task-actions">
          <el-button 
            type="danger" 
            @click="handleCancel"
            :disabled="task.status !== 'running'"
            v-if="task.status === 'running'"
          >
            <el-icon><Close /></el-icon>
            取消任务
          </el-button>
          <el-button 
            type="danger" 
            @click="handleDelete"
            :disabled="task.status === 'running'"
          >
            <el-icon><Delete /></el-icon>
            删除任务
          </el-button>
          <el-button 
            type="primary" 
            @click="goToResults"
            :disabled="task.status !== 'completed'"
            v-if="task.status === 'completed'"
          >
            <el-icon><View /></el-icon>
            查看结果
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 进度跟踪 -->
    <el-card class="progress-card" v-if="task && task.status === 'running'">
      <template #header>
        <h3>处理进度</h3>
      </template>
      
      <div class="progress-content">
        <el-progress 
          :percentage="progress" 
          :status="progressStatus"
          :stroke-width="8"
        />
        <p class="progress-message">{{ progressMessage }}</p>
        
        <div class="progress-steps">
          <el-steps :active="currentStep" finish-status="success">
            <el-step title="问题分析" description="分析问题类型和约束" />
            <el-step title="代码生成" description="生成解决方案代码" />
            <el-step title="代码执行" description="执行代码并生成结果" />
            <el-step title="论文撰写" description="生成完整论文" />
            <el-step title="保存结果" description="保存所有输出文件" />
          </el-steps>
        </div>
      </div>
    </el-card>

    <!-- 分析结果 -->
    <el-card class="analysis-card" v-if="task && task.analysis_result">
      <template #header>
        <h3>问题分析结果</h3>
      </template>
      
      <div class="analysis-content">
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="analysis-item">
              <h4>问题类型</h4>
              <p>{{ task.analysis_result.problem_type || '未知' }}</p>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="analysis-item">
              <h4>目标</h4>
              <p>{{ task.analysis_result.objective || '待分析' }}</p>
            </div>
          </el-col>
        </el-row>
        
        <div class="analysis-item" v-if="task.analysis_result.variables?.length">
          <h4>变量定义</h4>
          <el-table :data="task.analysis_result.variables" size="small">
            <el-table-column prop="name" label="变量名" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </div>
        
        <div class="analysis-item" v-if="task.analysis_result.constraints?.length">
          <h4>约束条件</h4>
          <ul>
            <li v-for="constraint in task.analysis_result.constraints" :key="constraint">
              {{ constraint }}
            </li>
          </ul>
        </div>
        
        <div class="analysis-item" v-if="task.analysis_result.algorithms?.length">
          <h4>推荐算法</h4>
          <el-tag 
            v-for="algorithm in task.analysis_result.algorithms" 
            :key="algorithm"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ algorithm }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <!-- 代码生成结果 -->
    <el-card class="code-card" v-if="task && task.code_generated">
      <template #header>
        <div class="card-header">
          <h3>生成的代码</h3>
          <el-button size="small" @click="copyCode">
            <el-icon><CopyDocument /></el-icon>
            复制代码
          </el-button>
        </div>
      </template>
      
      <div class="code-content">
        <pre><code>{{ task.code_generated }}</code></pre>
      </div>
    </el-card>

    <!-- 执行结果 -->
    <el-card class="execution-card" v-if="task && task.execution_result">
      <template #header>
        <h3>代码执行结果</h3>
      </template>
      
      <div class="execution-content">
        <div class="execution-status">
          <el-tag :type="task.execution_result.success ? 'success' : 'danger'">
            {{ task.execution_result.success ? '执行成功' : '执行失败' }}
          </el-tag>
          <span class="execution-time">
            执行时间: {{ task.execution_result.execution_time?.toFixed(2) }}秒
          </span>
        </div>
        
        <div class="execution-output" v-if="task.execution_result.output">
          <h4>输出结果</h4>
          <pre>{{ task.execution_result.output }}</pre>
        </div>
        
        <div class="execution-error" v-if="task.execution_result.error">
          <h4>错误信息</h4>
          <pre class="error-text">{{ task.execution_result.error }}</pre>
        </div>
      </div>
    </el-card>

    <!-- 错误信息 -->
    <el-card class="error-card" v-if="task && task.error_message">
      <template #header>
        <h3>错误信息</h3>
      </template>
      
      <div class="error-content">
        <el-alert
          :title="task.error_message"
          type="error"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { WebSocketManager } from '../services/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()

const task = ref(null)
const loading = ref(false)
const progress = ref(0)
const progressMessage = ref('')
const currentStep = ref(0)
const wsManager = ref(null)

// 计算属性
const progressStatus = computed(() => {
  if (progress.value === 100) return 'success'
  if (progress.value > 0) return 'active'
  return 'exception'
})

// 获取任务详情
const fetchTask = async () => {
  const taskId = route.params.id
  if (!taskId) return
  
  loading.value = true
  try {
    await taskStore.fetchTask(taskId)
    task.value = taskStore.currentTask
    
    // 如果任务正在运行，建立WebSocket连接
    if (task.value?.status === 'running') {
      connectWebSocket(taskId)
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 建立WebSocket连接
const connectWebSocket = (taskId) => {
  wsManager.value = new WebSocketManager()
  
  wsManager.value.connect(
    taskId,
    (data) => {
      // 处理WebSocket消息
      if (data.type === 'progress') {
        progress.value = data.progress
        progressMessage.value = data.message
        currentStep.value = Math.floor(data.progress / 20)
      } else if (data.type === 'status') {
        if (data.status === 'completed') {
          progress.value = 100
          progressMessage.value = '任务完成！'
          currentStep.value = 5
          // 重新获取任务详情
          fetchTask()
        }
      } else if (data.type === 'error') {
        ElMessage.error(data.error)
      }
    },
    (error) => {
      console.error('WebSocket连接错误:', error)
    },
    () => {
      console.log('WebSocket连接关闭')
    }
  )
}

// 取消任务
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个任务吗？', '确认取消', {
      type: 'warning'
    })
    
    await taskStore.cancelTask(task.value.id)
    ElMessage.success('任务已取消')
    
    // 重新获取任务详情
    await fetchTask()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败:', error)
      ElMessage.error('取消任务失败')
    }
  }
}

// 删除任务
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？删除后无法恢复。', '确认删除', {
      type: 'warning'
    })
    
    await taskStore.deleteTask(task.value.id)
    ElMessage.success('任务已删除')
    
    // 跳转到任务列表
    router.push('/tasks')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 查看结果
const goToResults = () => {
  router.push(`/results/${task.value.id}`)
}

// 复制代码
const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(task.value.code_generated)
    ElMessage.success('代码已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 获取状态标签类型
const getStatusTag = (status) => {
  const tags = {
    'pending': 'info',
    'running': 'warning',
    'completed': 'success',
    'failed': 'danger',
    'cancelled': 'info'
  }
  return tags[status] || 'default'
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const labels = {
    'pending': '等待中',
    'running': '运行中',
    'completed': '已完成',
    'failed': '失败',
    'cancelled': '已取消'
  }
  return labels[status] || status
}

// 获取任务类型标签
const getTaskTypeTag = (type) => {
  const tags = {
    'math_modeling': 'primary',
    'data_analysis': 'success',
    'optimization': 'warning',
    'simulation': 'info'
  }
  return tags[type] || 'default'
}

// 获取任务类型标签
const getTaskTypeLabel = (type) => {
  const labels = {
    'math_modeling': '数学建模',
    'data_analysis': '数据分析',
    'optimization': '优化问题',
    'simulation': '仿真模拟'
  }
  return labels[type] || type
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchTask()
})

onUnmounted(() => {
  // 清理WebSocket连接
  if (wsManager.value) {
    wsManager.value.disconnectAll()
  }
})
</script>

<style lang="scss" scoped>
.task-detail-view {
  max-width: 1000px;
  margin: 0 auto;
}

.task-info-card {
  margin-bottom: 24px;
  
  .card-header {
    h2 {
      margin: 0 0 12px 0;
      color: #333;
    }
    
    .task-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .create-time {
        color: #666;
        font-size: 14px;
      }
    }
  }
  
  .task-content {
    .task-description,
    .task-constraints {
      margin-bottom: 24px;
      
      h3 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 16px;
      }
      
      p {
        margin: 0;
        color: #666;
        line-height: 1.6;
      }
    }
    
    .task-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.progress-card {
  margin-bottom: 24px;
  
  .progress-content {
    .progress-message {
      text-align: center;
      margin: 16px 0;
      color: #666;
    }
    
    .progress-steps {
      margin-top: 24px;
    }
  }
}

.analysis-card,
.code-card,
.execution-card,
.error-card {
  margin-bottom: 24px;
  
  .analysis-content {
    .analysis-item {
      margin-bottom: 24px;
      
      h4 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 14px;
      }
      
      p {
        margin: 0;
        color: #666;
        line-height: 1.5;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        
        li {
          color: #666;
          line-height: 1.5;
          margin-bottom: 4px;
        }
      }
    }
  }
  
  .code-content {
    pre {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 4px;
      overflow-x: auto;
      font-size: 14px;
      line-height: 1.5;
    }
  }
  
  .execution-content {
    .execution-status {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
      
      .execution-time {
        color: #666;
        font-size: 14px;
      }
    }
    
    .execution-output,
    .execution-error {
      margin-bottom: 16px;
      
      h4 {
        margin: 0 0 8px 0;
        color: #333;
        font-size: 14px;
      }
      
      pre {
        background: #f5f7fa;
        padding: 12px;
        border-radius: 4px;
        overflow-x: auto;
        font-size: 14px;
        line-height: 1.5;
        
        &.error-text {
          background: #fef0f0;
          color: #f56c6c;
        }
      }
    }
  }
}

.loading-state {
  margin: 24px 0;
}
</style>
