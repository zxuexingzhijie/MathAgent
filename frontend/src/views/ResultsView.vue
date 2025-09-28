<template>
  <div class="results-view">
    <div class="page-header">
      <h1>结果查看</h1>
      <p>查看所有任务的执行结果和报告</p>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="任务类型">
          <el-select v-model="filters.type" placeholder="选择类型" clearable>
            <el-option
              v-for="type in taskTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option
              v-for="status in taskStatuses"
              :key="status.value"
              :label="status.label"
              :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilters">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结果列表 -->
    <el-card class="results-card">
      <template #header>
        <span>任务结果列表</span>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="filteredTasks.length === 0" class="empty-state">
        <el-empty description="暂无结果数据" />
      </div>

      <div v-else class="results-list">
        <div
          v-for="task in filteredTasks"
          :key="task.id"
          class="result-item"
          @click="viewTaskDetail(task)"
        >
          <div class="result-header">
            <h3>{{ task.title }}</h3>
            <div class="result-meta">
              <el-tag :type="getTypeTagType(task.type)">
                {{ getTypeLabel(task.type) }}
              </el-tag>
              <el-tag :type="getStatusTagType(task.status)">
                {{ getStatusLabel(task.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="result-content">
            <p class="description">{{ task.description || '暂无描述' }}</p>
            <div class="result-info">
              <span class="created-time">
                创建时间: {{ formatDate(task.createdAt) }}
              </span>
              <span v-if="task.completedAt" class="completed-time">
                完成时间: {{ formatDate(task.completedAt) }}
              </span>
              <span v-if="task.executionTimeMs" class="execution-time">
                执行时间: {{ formatDuration(task.executionTimeMs) }}
              </span>
            </div>
        </div>
        
          <div class="result-actions">
            <el-button size="small" type="primary" @click.stop="viewTaskDetail(task)">
              查看详情
            </el-button>
            <el-button
              v-if="task.status === 'COMPLETED'"
              size="small"
              type="success"
              @click.stop="downloadReport(task)"
            >
              下载报告
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const taskStore = useTaskStore()

// 响应式数据
const loading = ref(true)
const filters = reactive({
  type: '',
  status: ''
})

// 任务类型和状态选项
const taskTypes = [
  { value: 'OPTIMIZATION', label: '优化问题' },
  { value: 'PREDICTION', label: '预测问题' },
  { value: 'CLASSIFICATION', label: '分类问题' },
  { value: 'SIMULATION', label: '仿真问题' },
  { value: 'STATISTICAL_ANALYSIS', label: '统计分析' },
  { value: 'MACHINE_LEARNING', label: '机器学习' },
  { value: 'COMPLEX_SYSTEM', label: '复杂系统' },
  { value: 'OTHER', label: '其他' }
]

const taskStatuses = [
  { value: 'COMPLETED', label: '已完成' },
  { value: 'FAILED', label: '失败' },
  { value: 'CANCELLED', label: '已取消' }
]

// 计算属性
const filteredTasks = computed(() => {
  let tasks = taskStore.tasks.filter(task => 
    task.status === 'COMPLETED' || task.status === 'FAILED' || task.status === 'CANCELLED'
  )

  if (filters.type) {
    tasks = tasks.filter(task => task.type === filters.type)
  }

  if (filters.status) {
    tasks = tasks.filter(task => task.status === filters.status)
  }

  return tasks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

// 方法
const applyFilters = () => {
  // 筛选逻辑已在计算属性中实现
}

const resetFilters = () => {
  filters.type = ''
  filters.status = ''
}

const viewTaskDetail = (task) => {
  router.push(`/task/${task.id}`)
}

const downloadReport = async (task) => {
  try {
    // 这里应该调用下载API
    ElMessage.success('报告下载功能开发中...')
  } catch (error) {
    ElMessage.error('下载报告失败: ' + error.message)
  }
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
  const typeObj = taskTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}

const getStatusTagType = (status) => {
  const statusMap = {
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const statusObj = taskStatuses.find(s => s.value === status)
  return statusObj ? statusObj.label : status
}

const formatDate = (dateString) => {
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
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
onMounted(async () => {
  await taskStore.loadTasks()
  loading.value = false
})
</script>

<style lang="scss" scoped>
.results-view {
.page-header {
  margin-bottom: 24px;
  
  h1 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 28px;
      font-weight: 600;
    }
    
    p {
    margin: 0;
      color: #606266;
      font-size: 16px;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .results-card {
    .loading-container {
      padding: 20px;
    }

    .empty-state {
      padding: 40px 0;
    }

    .results-list {
      .result-item {
        border: 1px solid #ebeef5;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #409eff;
          box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
        }

        .result-header {
    display: flex;
    justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;
    
    h3 {
      margin: 0;
            color: #303133;
            font-size: 18px;
            font-weight: 600;
            flex: 1;
    }
    
          .result-meta {
      display: flex;
      gap: 8px;
            flex-shrink: 0;
          }
        }

        .result-content {
        margin-bottom: 16px;

          .description {
            margin: 0 0 12px 0;
            color: #606266;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }

          .result-info {
            display: flex;
            gap: 16px;
        font-size: 14px;
            color: #909399;

            span {
              white-space: nowrap;
            }
          }
        }

        .result-actions {
          display: flex;
          gap: 8px;
          justify-content: flex-end;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .results-view {
    .results-card {
      .results-list {
        .result-item {
          .result-header {
            flex-direction: column;
            gap: 12px;

            .result-meta {
              align-self: flex-start;
            }
          }

          .result-content {
            .result-info {
              flex-direction: column;
              gap: 8px;
            }
          }

          .result-actions {
            justify-content: flex-start;
          }
        }
      }
    }
  }
}
</style>