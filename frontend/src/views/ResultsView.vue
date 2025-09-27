<template>
  <div class="results-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>任务结果</h1>
      <div class="header-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="downloadAll">
          <el-icon><Download /></el-icon>
          下载全部
        </el-button>
      </div>
    </div>

    <!-- 任务信息 -->
    <el-card class="task-info-card" v-if="task">
      <template #header>
        <h3>{{ task.title }}</h3>
      </template>
      
      <div class="task-info">
        <el-row :gutter="24">
          <el-col :span="8">
            <div class="info-item">
              <span class="label">任务类型:</span>
              <el-tag :type="getTaskTypeTag(task.task_type)">
                {{ getTaskTypeLabel(task.task_type) }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">完成时间:</span>
              <span>{{ formatTime(task.completed_at) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <span class="label">执行时间:</span>
              <span>{{ getExecutionTime() }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 结果导航 -->
    <el-card class="results-nav-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="论文" name="paper">
          <template #label>
            <span>
              <el-icon><Document /></el-icon>
              论文
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="代码" name="code">
          <template #label>
            <span>
              <el-icon><Code /></el-icon>
              代码
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="分析结果" name="analysis">
          <template #label>
            <span>
              <el-icon><DataAnalysis /></el-icon>
              分析结果
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="执行结果" name="execution">
          <template #label>
            <span>
              <el-icon><Monitor /></el-icon>
              执行结果
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 论文内容 -->
    <el-card class="content-card" v-if="activeTab === 'paper' && task?.paper_content">
      <template #header>
        <div class="card-header">
          <h3>生成的论文</h3>
          <div class="header-actions">
            <el-button size="small" @click="copyPaper">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button size="small" type="primary" @click="downloadPaper">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="paper-content">
        <div class="markdown-content" v-html="renderedPaper"></div>
      </div>
    </el-card>

    <!-- 代码内容 -->
    <el-card class="content-card" v-if="activeTab === 'code' && task?.code_generated">
      <template #header>
        <div class="card-header">
          <h3>生成的代码</h3>
          <div class="header-actions">
            <el-button size="small" @click="copyCode">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
            <el-button size="small" type="primary" @click="downloadCode">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="code-content">
        <pre><code class="python">{{ task.code_generated }}</code></pre>
      </div>
    </el-card>

    <!-- 分析结果 -->
    <el-card class="content-card" v-if="activeTab === 'analysis' && task?.analysis_result">
      <template #header>
        <h3>问题分析结果</h3>
      </template>
      
      <div class="analysis-content">
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="analysis-section">
              <h4>基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">问题类型:</span>
                  <span>{{ task.analysis_result.problem_type || '未知' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">目标:</span>
                  <span>{{ task.analysis_result.objective || '待分析' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">复杂度:</span>
                  <span>{{ task.analysis_result.complexity || '中等' }}</span>
                </div>
              </div>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="analysis-section">
              <h4>推荐算法</h4>
              <div class="algorithms">
                <el-tag 
                  v-for="algorithm in task.analysis_result.algorithms" 
                  :key="algorithm"
                  style="margin-right: 8px; margin-bottom: 8px;"
                >
                  {{ algorithm }}
                </el-tag>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="analysis-section" v-if="task.analysis_result.variables?.length">
          <h4>变量定义</h4>
          <el-table :data="task.analysis_result.variables" size="small">
            <el-table-column prop="name" label="变量名" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </div>
        
        <div class="analysis-section" v-if="task.analysis_result.constraints?.length">
          <h4>约束条件</h4>
          <ul class="constraints-list">
            <li v-for="constraint in task.analysis_result.constraints" :key="constraint">
              {{ constraint }}
            </li>
          </ul>
        </div>
        
        <div class="analysis-section" v-if="task.analysis_result.evaluation_metrics?.length">
          <h4>评估指标</h4>
          <div class="metrics">
            <el-tag 
              v-for="metric in task.analysis_result.evaluation_metrics" 
              :key="metric"
              type="info"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ metric }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 执行结果 -->
    <el-card class="content-card" v-if="activeTab === 'execution' && task?.execution_result">
      <template #header>
        <h3>代码执行结果</h3>
      </template>
      
      <div class="execution-content">
        <div class="execution-summary">
          <el-row :gutter="24">
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">执行状态</div>
                <div class="summary-value">
                  <el-tag :type="task.execution_result.success ? 'success' : 'danger'">
                    {{ task.execution_result.success ? '成功' : '失败' }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">执行时间</div>
                <div class="summary-value">{{ task.execution_result.execution_time?.toFixed(2) }}秒</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">返回码</div>
                <div class="summary-value">{{ task.execution_result.return_code || 'N/A' }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div class="execution-output" v-if="task.execution_result.output">
          <h4>输出结果</h4>
          <div class="output-content">
            <pre>{{ task.execution_result.output }}</pre>
          </div>
        </div>
        
        <div class="execution-error" v-if="task.execution_result.error">
          <h4>错误信息</h4>
          <div class="error-content">
            <pre class="error-text">{{ task.execution_result.error }}</pre>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card class="empty-card" v-if="!hasContent">
      <el-empty description="暂无结果数据" />
    </el-card>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()

const task = ref(null)
const loading = ref(false)
const activeTab = ref('paper')
const markdown = new MarkdownIt({
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value
      } catch (__) {}
    }
    return ''
  }
})

// 计算属性
const renderedPaper = computed(() => {
  if (!task.value?.paper_content) return ''
  return markdown.render(task.value.paper_content)
})

const hasContent = computed(() => {
  return task.value && (
    task.value.paper_content ||
    task.value.code_generated ||
    task.value.analysis_result ||
    task.value.execution_result
  )
})

// 获取任务详情
const fetchTask = async () => {
  const taskId = route.params.taskId
  if (!taskId) return
  
  loading.value = true
  try {
    await taskStore.fetchTask(taskId)
    task.value = taskStore.currentTask
    
    // 根据可用内容设置默认标签页
    if (task.value) {
      if (task.value.paper_content) {
        activeTab.value = 'paper'
      } else if (task.value.code_generated) {
        activeTab.value = 'code'
      } else if (task.value.analysis_result) {
        activeTab.value = 'analysis'
      } else if (task.value.execution_result) {
        activeTab.value = 'execution'
      }
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 复制论文
const copyPaper = async () => {
  try {
    await navigator.clipboard.writeText(task.value.paper_content)
    ElMessage.success('论文内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
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

// 下载论文
const downloadPaper = () => {
  const content = task.value.paper_content
  const blob = new Blob([content], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${task.value.title}_论文.md`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('论文下载成功')
}

// 下载代码
const downloadCode = () => {
  const content = task.value.code_generated
  const blob = new Blob([content], { type: 'text/python' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${task.value.title}_代码.py`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('代码下载成功')
}

// 下载全部
const downloadAll = () => {
  // 创建ZIP文件（这里简化处理，实际应该调用后端API）
  ElMessage.info('下载全部功能开发中...')
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

// 获取执行时间
const getExecutionTime = () => {
  if (!task.value?.started_at || !task.value?.completed_at) return 'N/A'
  
  const start = new Date(task.value.started_at)
  const end = new Date(task.value.completed_at)
  const diff = end - start
  
  const minutes = Math.floor(diff / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  
  return `${minutes}分${seconds}秒`
}

// 生命周期
onMounted(() => {
  fetchTask()
})
</script>

<style lang="scss" scoped>
.results-view {
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
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.task-info-card {
  margin-bottom: 24px;
  
  .task-info {
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .label {
        font-weight: 500;
        color: #666;
      }
    }
  }
}

.results-nav-card {
  margin-bottom: 24px;
}

.content-card {
  margin-bottom: 24px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
      color: #333;
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .paper-content {
    .markdown-content {
      line-height: 1.6;
      
      :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
        margin-top: 24px;
        margin-bottom: 16px;
        color: #333;
      }
      
      :deep(p) {
        margin-bottom: 16px;
        color: #666;
      }
      
      :deep(code) {
        background: #f5f7fa;
        padding: 2px 4px;
        border-radius: 3px;
        font-size: 14px;
      }
      
      :deep(pre) {
        background: #f5f7fa;
        padding: 16px;
        border-radius: 4px;
        overflow-x: auto;
        margin-bottom: 16px;
      }
      
      :deep(blockquote) {
        border-left: 4px solid #409eff;
        padding-left: 16px;
        margin: 16px 0;
        color: #666;
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
      
      code {
        background: none;
        padding: 0;
      }
    }
  }
  
  .analysis-content {
    .analysis-section {
      margin-bottom: 32px;
      
      h4 {
        margin: 0 0 16px 0;
        color: #333;
        font-size: 16px;
        border-bottom: 2px solid #409eff;
        padding-bottom: 8px;
      }
      
      .info-grid {
        display: grid;
        gap: 12px;
        
        .info-item {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .label {
            font-weight: 500;
            color: #666;
            min-width: 80px;
          }
        }
      }
      
      .algorithms,
      .metrics {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
      
      .constraints-list {
        margin: 0;
        padding-left: 20px;
        
        li {
          color: #666;
          line-height: 1.5;
          margin-bottom: 8px;
        }
      }
    }
  }
  
  .execution-content {
    .execution-summary {
      margin-bottom: 24px;
      
      .summary-item {
        text-align: center;
        
        .summary-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }
        
        .summary-value {
          font-size: 18px;
          font-weight: bold;
          color: #333;
        }
      }
    }
    
    .execution-output,
    .execution-error {
      margin-bottom: 24px;
      
      h4 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 16px;
      }
      
      .output-content,
      .error-content {
        pre {
          background: #f5f7fa;
          padding: 16px;
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
}

.empty-card {
  text-align: center;
  padding: 40px 0;
}

.loading-state {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
}
</style>
