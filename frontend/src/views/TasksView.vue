<template>
  <div class="tasks-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>任务列表</h1>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon>
        创建新任务
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="状态筛选">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="等待中" value="pending" />
            <el-option label="运行中" value="running" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="类型筛选">
          <el-select v-model="filterForm.task_type" placeholder="全部类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="数学建模" value="math_modeling" />
            <el-option label="数据分析" value="data_analysis" />
            <el-option label="优化问题" value="optimization" />
            <el-option label="仿真模拟" value="simulation" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.search"
            placeholder="搜索任务标题或描述"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="tasks-card">
      <template #header>
        <div class="card-header">
          <span>任务列表 ({{ taskStore.taskCount }} 个任务)</span>
          <el-button size="small" @click="refreshTasks">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="taskStore.loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 任务表格 -->
      <el-table 
        v-else-if="taskStore.tasks.length > 0"
        :data="taskStore.tasks" 
        stripe
        @row-click="goToTaskDetail"
        style="cursor: pointer;"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="task-title">
              <span>{{ row.title }}</span>
              <el-tag 
                :type="getTaskTypeTag(row.task_type)" 
                size="small"
                style="margin-left: 8px;"
              >
                {{ getTaskTypeLabel(row.task_type) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.created_at) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="updated_at" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updated_at) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                size="small" 
                @click.stop="goToTaskDetail(row.id)"
              >
                查看
              </el-button>
              
              <el-button 
                size="small" 
                type="danger" 
                @click.stop="handleCancel(row)"
                :disabled="row.status !== 'running'"
                v-if="row.status === 'running'"
              >
                取消
              </el-button>
              
              <el-button 
                size="small" 
                type="success" 
                @click.stop="goToResults(row.id)"
                :disabled="row.status !== 'completed'"
                v-if="row.status === 'completed'"
              >
                结果
              </el-button>
              
              <el-button 
                size="small" 
                type="danger" 
                @click.stop="handleDelete(row)"
                :disabled="row.status === 'running'"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-else description="暂无任务" />
    </el-card>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="taskStore.tasks.length > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalCount"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const taskStore = useTaskStore()

// 筛选表单
const filterForm = reactive({
  status: '',
  task_type: '',
  search: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

const totalCount = ref(0)

// 获取任务列表
const fetchTasks = async () => {
  try {
    const params = {
      skip: (pagination.page - 1) * pagination.size,
      limit: pagination.size
    }
    
    if (filterForm.status) {
      params.status = filterForm.status
    }
    
    await taskStore.fetchTasks(params)
    totalCount.value = taskStore.tasks.length // 这里应该从API返回总数
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchTasks()
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = ''
  filterForm.task_type = ''
  filterForm.search = ''
  pagination.page = 1
  fetchTasks()
}

// 刷新任务列表
const refreshTasks = () => {
  fetchTasks()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchTasks()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchTasks()
}

// 跳转到创建任务页面
const goToCreate = () => {
  router.push('/create')
}

// 跳转到任务详情
const goToTaskDetail = (taskId) => {
  router.push(`/task/${taskId}`)
}

// 跳转到结果页面
const goToResults = (taskId) => {
  router.push(`/results/${taskId}`)
}

// 取消任务
const handleCancel = async (task) => {
  try {
    await ElMessageBox.confirm('确定要取消这个任务吗？', '确认取消', {
      type: 'warning'
    })
    
    await taskStore.cancelTask(task.id)
    ElMessage.success('任务已取消')
    
    // 刷新列表
    await fetchTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败:', error)
      ElMessage.error('取消任务失败')
    }
  }
}

// 删除任务
const handleDelete = async (task) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？删除后无法恢复。', '确认删除', {
      type: 'warning'
    })
    
    await taskStore.deleteTask(task.id)
    ElMessage.success('任务已删除')
    
    // 刷新列表
    await fetchTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
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

// 监听筛选条件变化
watch([() => filterForm.status, () => filterForm.task_type], () => {
  pagination.page = 1
  fetchTasks()
})

// 生命周期
onMounted(() => {
  fetchTasks()
})
</script>

<style lang="scss" scoped>
.tasks-view {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  h1 {
    margin: 0;
    color: #333;
  }
}

.filter-card {
  margin-bottom: 24px;
}

.tasks-card {
  margin-bottom: 24px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .task-title {
    display: flex;
    align-items: center;
  }
  
  .action-buttons {
    display: flex;
    gap: 8px;
  }
}

.loading-state {
  padding: 20px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
