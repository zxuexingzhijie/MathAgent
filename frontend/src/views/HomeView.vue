<template>
  <div class="home-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>数学建模任务管理</h1>
      <p>管理和监控您的数学建模研究任务</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ taskStore.taskCount }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon running">
                <el-icon><Loading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ taskStore.runningTasks.length }}</div>
                <div class="stat-label">执行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ taskStore.completedTasks.length }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon failed">
                <el-icon><Close /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ taskStore.failedTasks.length }}</div>
                <div class="stat-label">失败</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshTasks">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="createNewTask">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="taskStore.loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 任务表格 -->
      <el-table
        v-else
        :data="taskStore.tasks"
        style="width: 100%"
        @row-click="handleRowClick"
        class="task-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionTimeMs" label="执行时间" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.executionTimeMs) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                type="primary"
                @click.stop="viewTask(row)"
              >
                查看
              </el-button>
              <el-button
                v-if="canExecute(row.status)"
                size="small"
                type="success"
                @click.stop="executeTask(row)"
                :loading="executingTasks.has(row.id)"
              >
                执行
              </el-button>
              <el-button
                v-if="canCancel(row.status)"
                size="small"
                type="warning"
                @click.stop="cancelTask(row)"
              >
                取消
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!taskStore.loading && taskStore.tasks.length === 0" class="empty-state">
        <el-empty description="暂无任务">
          <el-button type="primary" @click="createNewTask">
            创建第一个任务
          </el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const taskStore = useTaskStore()

// 响应式数据
const executingTasks = ref(new Set())

// 方法
const refreshTasks = async () => {
  await taskStore.loadTasks()
  ElMessage.success('任务列表已刷新')
}

const createNewTask = () => {
  router.push('/create')
}

const handleRowClick = (row) => {
  viewTask(row)
}

const viewTask = (task) => {
  router.push(`/task/${task.id}`)
}

const executeTask = async (task) => {
  try {
    executingTasks.value.add(task.id)
    await taskStore.executeTask(task.id)
    ElMessage.success('任务执行已开始')
  } catch (error) {
    ElMessage.error('执行任务失败: ' + error.message)
  } finally {
    executingTasks.value.delete(task.id)
  }
}

const cancelTask = async (task) => {
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
    
    await taskStore.cancelTask(task.id)
    ElMessage.success('任务已取消')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消任务失败: ' + error.message)
    }
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

const canExecute = (status) => {
  return ['CREATED', 'FAILED'].includes(status)
}

const canCancel = (status) => {
  return ['ANALYZING', 'DATA_COLLECTING', 'MODEL_BUILDING', 'SOLVING', 'GENERATING_REPORT'].includes(status)
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
onMounted(() => {
  taskStore.loadTasks()
})
</script>

<style lang="scss" scoped>
.home-view {
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

  .stats-cards {
    margin-bottom: 24px;
    
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          font-size: 24px;
          color: white;
          
          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }
          
          &.running {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
          
          &.completed {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }
          
          &.failed {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          }
        }
        
        .stat-info {
          .stat-number {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
            margin-bottom: 4px;
          }
          
          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .task-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    .loading-container {
      padding: 20px;
    }
    
    .task-table {
      .el-table__row {
        cursor: pointer;
        
        &:hover {
          background-color: #f5f7fa;
        }
      }
    }
    
    .empty-state {
      padding: 40px 0;
    }
  }
}
</style>