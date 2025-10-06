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
