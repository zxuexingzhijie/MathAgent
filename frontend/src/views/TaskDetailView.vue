<template>
  <div class="task-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 任务详情 -->
    <div v-else-if="task" class="task-content">
      <!-- 任务头部 -->
      <el-card class="task-header-card">
        <div class="task-header">
          <div class="task-info">
            <h1>{{ task.title }}</h1>
          <div class="task-meta">
              <el-tag :type="getTypeTagType(task.type)">
                {{ getTypeLabel(task.type) }}
              </el-tag>
              <el-tag :type="getStatusTagType(task.status)">
              {{ getStatusLabel(task.status) }}
            </el-tag>
              <span class="created-time">
                创建时间: {{ formatDate(task.createdAt) }}
            </span>
          </div>
        </div>
        <div class="task-actions">
            <el-button-group>
          <el-button 
                v-if="canExecute(task.status)"
                type="primary"
                @click="executeTask"
                :loading="executing"
              >
                <el-icon><PlayArrow /></el-icon>
                执行任务
          </el-button>
          <el-button 
                v-if="canCancel(task.status)"
                type="warning"
                @click="cancelTask"
              >
                <el-icon><Stop /></el-icon>
                取消任务
          </el-button>
              <el-button @click="refreshTask">
                <el-icon><Refresh /></el-icon>
                刷新
          </el-button>
            </el-button-group>
          </div>
      </div>
    </el-card>

      <!-- 任务内容 -->
      <el-row :gutter="20">
        <!-- 左侧内容 -->
        <el-col :span="16">
          <!-- 问题描述 -->
          <el-card class="content-card">
      <template #header>
              <span>问题描述</span>
      </template>
            <div class="problem-content">
              <div v-if="task.description" class="description">
                <h4>任务描述</h4>
                <p>{{ task.description }}</p>
              </div>
              <div class="problem-statement">
                <h4>问题陈述</h4>
                <div class="statement-text">{{ task.problemStatement }}</div>
              </div>
              <div v-if="task.researchGoals" class="research-goals">
                <h4>研究目标</h4>
                <p>{{ task.researchGoals }}</p>
        </div>
      </div>
    </el-card>

          <!-- 技术要求 -->
          <el-card class="content-card">
      <template #header>
              <span>技术要求</span>
      </template>
            <div class="technical-requirements">
              <div v-if="task.dataRequirements" class="requirement-item">
                <h4>数据需求</h4>
                <p>{{ task.dataRequirements }}</p>
              </div>
              <div v-if="task.modelConstraints" class="requirement-item">
                <h4>模型约束</h4>
                <p>{{ task.modelConstraints }}</p>
              </div>
              <div v-if="task.expectedOutputs" class="requirement-item">
                <h4>预期输出</h4>
                <p>{{ task.expectedOutputs }}</p>
            </div>
            </div>
          </el-card>

          <!-- 执行结果 -->
          <el-card v-if="results.length > 0" class="content-card">
            <template #header>
              <span>执行结果</span>
            </template>
            <div class="results-content">
              <el-timeline>
                <el-timeline-item
                  v-for="result in results"
                  :key="result.id"
                  :timestamp="formatDate(result.createdAt)"
                  :type="getResultTimelineType(result.status)"
                >
                  <div class="result-item">
                    <h4>{{ result.nodeName }}</h4>
                    <el-tag :type="getStatusTagType(result.status)">
                      {{ getStatusLabel(result.status) }}
          </el-tag>
                    <div v-if="result.outputData" class="output-data">
                      <el-collapse>
                        <el-collapse-item title="查看输出数据" name="output">
                          <pre>{{ result.outputData }}</pre>
                        </el-collapse-item>
                      </el-collapse>
                    </div>
                    <div v-if="result.errorMessage" class="error-message">
                      <el-alert
                        :title="result.errorMessage"
                        type="error"
                        :closable="false"
                      />
        </div>
      </div>
                </el-timeline-item>
              </el-timeline>
      </div>
    </el-card>
        </el-col>

        <!-- 右侧信息 -->
        <el-col :span="8">
          <!-- 执行状态 -->
          <el-card class="info-card">
      <template #header>
              <span>执行状态</span>
      </template>
            <div class="status-info">
              <div class="status-item">
                <span class="label">当前状态:</span>
                <el-tag :type="getStatusTagType(task.status)">
                  {{ getStatusLabel(task.status) }}
          </el-tag>
              </div>
              <div v-if="task.startedAt" class="status-item">
                <span class="label">开始时间:</span>
                <span>{{ formatDate(task.startedAt) }}</span>
              </div>
              <div v-if="task.completedAt" class="status-item">
                <span class="label">完成时间:</span>
                <span>{{ formatDate(task.completedAt) }}</span>
        </div>
              <div v-if="task.executionTimeMs" class="status-item">
                <span class="label">执行时间:</span>
                <span>{{ formatDuration(task.executionTimeMs) }}</span>
        </div>
              <div v-if="task.graphExecutionId" class="status-item">
                <span class="label">执行ID:</span>
                <span class="execution-id">{{ task.graphExecutionId }}</span>
        </div>
      </div>
    </el-card>

          <!-- 执行日志 -->
          <el-card class="info-card">
      <template #header>
              <span>执行日志</span>
      </template>
            <div class="logs-content">
              <el-scrollbar height="300px">
                <div v-if="logs.length > 0" class="log-list">
                  <div
                    v-for="log in logs"
                    :key="log.id"
                    class="log-item"
                    :class="`log-${log.level.toLowerCase()}`"
                  >
                    <div class="log-header">
                      <span class="log-time">{{ formatTime(log.createdAt) }}</span>
                      <el-tag :type="getLogLevelTagType(log.level)" size="small">
                        {{ log.level }}
                      </el-tag>
                    </div>
                    <div class="log-message">{{ log.message }}</div>
                    <div v-if="log.nodeName" class="log-node">
                      [{{ log.nodeName }}]
                    </div>
                  </div>
                </div>
                <div v-else class="no-logs">
                  <el-empty description="暂无日志" :image-size="80" />
                </div>
              </el-scrollbar>
      </div>
    </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <el-result
        icon="error"
        title="任务不存在"
        sub-title="请检查任务ID是否正确"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">
            返回任务列表
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()

// 响应式数据
const loading = ref(true)
const executing = ref(false)
const task = ref(null)
const results = ref([])
const logs = ref([])

// 计算属性
const taskId = computed(() => parseInt(route.params.id))

// 方法
const loadTaskData = async () => {
  try {
    loading.value = true
    
    // 并行加载任务详情、结果和日志
    const [taskData, resultsData, logsData] = await Promise.all([
      taskStore.getTaskDetail(taskId.value),
      taskStore.getTaskResults(taskId.value),
      taskStore.getTaskLogs(taskId.value)
    ])
    
    task.value = taskData
    results.value = resultsData
    logs.value = logsData
    
  } catch (error) {
    ElMessage.error('加载任务数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const executeTask = async () => {
  try {
    executing.value = true
    await taskStore.executeTask(taskId.value)
    ElMessage.success('任务执行已开始')
    await loadTaskData() // 重新加载数据
  } catch (error) {
    ElMessage.error('执行任务失败: ' + error.message)
  } finally {
    executing.value = false
  }
}

const cancelTask = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个任务吗？',
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      type: 'warning'
      }
    )
    
    await taskStore.cancelTask(taskId.value)
    ElMessage.success('任务已取消')
    await loadTaskData() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消任务失败: ' + error.message)
    }
  }
}

const refreshTask = async () => {
  await loadTaskData()
  ElMessage.success('数据已刷新')
}

// 工具方法
const getTypeTagType = (type) => {
  const typeMap = {
    'OPTIMIZATION': 'success',
    'PREDICTION': 'primary',
    'CLASSIFICATION': 'info',
    'SIMULATION': 'warning',
    'STATISTICAL_ANALYSIS': 'danger',
    'MACHINE_LEARNING': 'success',
    'COMPLEX_SYSTEM': 'primary',
    'OTHER': 'info'
  }
  return typeMap[type] || 'info'
}

const getTypeLabel = (type) => {
  const labelMap = {
    'OPTIMIZATION': '优化问题',
    'PREDICTION': '预测问题',
    'CLASSIFICATION': '分类问题',
    'SIMULATION': '仿真问题',
    'STATISTICAL_ANALYSIS': '统计分析',
    'MACHINE_LEARNING': '机器学习',
    'COMPLEX_SYSTEM': '复杂系统',
    'OTHER': '其他'
  }
  return labelMap[type] || type
}

const getStatusTagType = (status) => {
  const statusMap = {
    'CREATED': 'info',
    'ANALYZING': 'warning',
    'DATA_COLLECTING': 'warning',
    'MODEL_BUILDING': 'warning',
    'SOLVING': 'warning',
    'GENERATING_REPORT': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    'CREATED': '已创建',
    'ANALYZING': '分析中',
    'DATA_COLLECTING': '数据收集中',
    'MODEL_BUILDING': '模型构建中',
    'SOLVING': '求解中',
    'GENERATING_REPORT': '生成报告中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return labelMap[status] || status
}

const getResultTimelineType = (status) => {
  const typeMap = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'RUNNING': 'warning',
    'PENDING': 'info',
    'SKIPPED': 'info'
  }
  return typeMap[status] || 'info'
}

const getLogLevelTagType = (level) => {
  const typeMap = {
    'DEBUG': 'info',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger',
    'FATAL': 'danger'
  }
  return typeMap[level] || 'info'
}

const canExecute = (status) => {
  return ['CREATED', 'FAILED'].includes(status)
}

const canCancel = (status) => {
  return ['ANALYZING', 'DATA_COLLECTING', 'MODEL_BUILDING', 'SOLVING', 'GENERATING_REPORT'].includes(status)
}

const formatDate = (dateString) => {
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

const formatTime = (dateString) => {
  return dayjs(dateString).format('HH:mm:ss')
}

const formatDuration = (ms) => {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}h ${minutes % 60}m`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`
  } else {
    return `${seconds}s`
  }
}

// 生命周期
onMounted(() => {
  loadTaskData()
})
</script>

<style lang="scss" scoped>
.task-detail-view {
  .loading-container {
    padding: 20px;
  }

  .task-content {
    .task-header-card {
      margin-bottom: 20px;
      
      .task-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .task-info {
          flex: 1;
          
          h1 {
      margin: 0 0 12px 0;
            color: #303133;
            font-size: 24px;
            font-weight: 600;
    }
    
    .task-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      
            .created-time {
              color: #909399;
        font-size: 14px;
      }
    }
  }
  
        .task-actions {
          flex-shrink: 0;
        }
      }
    }

    .content-card {
      margin-bottom: 20px;
      
      .problem-content {
        .description, .problem-statement, .research-goals {
          margin-bottom: 20px;
          
          h4 {
            margin: 0 0 8px 0;
            color: #303133;
        font-size: 16px;
            font-weight: 600;
      }
      
      p {
        margin: 0;
            color: #606266;
            line-height: 1.6;
          }
          
          .statement-text {
            background: #f8f9fa;
            padding: 16px;
            border-radius: 8px;
            border-left: 4px solid #409eff;
            color: #606266;
        line-height: 1.6;
            white-space: pre-wrap;
          }
        }
      }
      
      .technical-requirements {
        .requirement-item {
          margin-bottom: 16px;
          
          h4 {
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 14px;
            font-weight: 600;
          }
          
          p {
            margin: 0;
            color: #606266;
            line-height: 1.6;
            background: #f8f9fa;
            padding: 12px;
            border-radius: 6px;
          }
        }
      }
      
      .results-content {
        .result-item {
          h4 {
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 16px;
            font-weight: 600;
          }
          
          .output-data {
            margin-top: 12px;
            
            pre {
              background: #f8f9fa;
              padding: 12px;
              border-radius: 6px;
              font-size: 12px;
              overflow-x: auto;
            }
          }
          
          .error-message {
            margin-top: 12px;
          }
        }
      }
    }

    .info-card {
      margin-bottom: 20px;
      
      .status-info {
        .status-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .label {
            color: #606266;
            font-weight: 500;
          }
          
          .execution-id {
            font-family: monospace;
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .logs-content {
        .log-list {
          .log-item {
            margin-bottom: 12px;
            padding: 8px;
            border-radius: 6px;
            border-left: 3px solid transparent;
            
            &.log-error, &.log-fatal {
              background: #fef0f0;
              border-left-color: #f56c6c;
            }
            
            &.log-warn {
              background: #fdf6ec;
              border-left-color: #e6a23c;
            }
            
            &.log-info {
              background: #f0f9ff;
              border-left-color: #409eff;
            }
            
            &.log-debug {
              background: #f8f9fa;
              border-left-color: #909399;
            }
            
            .log-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 4px;
              
              .log-time {
                font-size: 12px;
                color: #909399;
                font-family: monospace;
              }
            }
            
            .log-message {
              color: #606266;
              font-size: 14px;
              line-height: 1.4;
            }
            
            .log-node {
              color: #909399;
              font-size: 12px;
              margin-top: 4px;
            }
          }
        }
        
        .no-logs {
          text-align: center;
          padding: 20px;
        }
      }
    }
  }

  .error-container {
    padding: 40px 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .task-detail-view {
    .task-content {
      .task-header-card {
        .task-header {
          flex-direction: column;
      gap: 16px;
          
          .task-actions {
            width: 100%;
            
            .el-button-group {
              width: 100%;
              
              .el-button {
                flex: 1;
              }
            }
          }
        }
      }
    }
  }
}
</style>