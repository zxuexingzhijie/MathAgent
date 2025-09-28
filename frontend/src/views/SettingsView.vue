<template>
  <div class="settings-view">
    <div class="page-header">
      <h1>系统设置</h1>
      <p>配置系统参数和AI模型设置</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧设置面板 -->
      <el-col :span="16">
        <!-- AI模型设置 -->
        <el-card class="settings-card">
          <template #header>
            <span>AI模型设置</span>
          </template>
          
          <el-form :model="aiSettings" label-width="120px">
            <el-form-item label="API密钥">
              <el-input
                v-model="aiSettings.apiKey"
                type="password"
                placeholder="请输入DashScope API密钥"
                show-password
              />
              <div class="form-tip">
                请确保API密钥有效且有足够的额度
              </div>
            </el-form-item>

            <el-form-item label="聊天模型">
              <el-select v-model="aiSettings.chatModel" style="width: 100%">
                <el-option label="qwen-max" value="qwen-max" />
                <el-option label="qwen-plus" value="qwen-plus" />
                <el-option label="qwen-turbo" value="qwen-turbo" />
              </el-select>
            </el-form-item>

            <el-form-item label="嵌入模型">
              <el-select v-model="aiSettings.embeddingModel" style="width: 100%">
                <el-option label="text-embedding-v3" value="text-embedding-v3" />
                <el-option label="text-embedding-v2" value="text-embedding-v2" />
              </el-select>
            </el-form-item>

            <el-form-item label="温度参数">
              <el-slider
                v-model="aiSettings.temperature"
                :min="0"
                :max="1"
                :step="0.1"
                show-input
              />
              <div class="form-tip">
                控制生成文本的随机性，值越高越随机
              </div>
            </el-form-item>

            <el-form-item label="最大令牌数">
              <el-input-number
                v-model="aiSettings.maxTokens"
                :min="100"
                :max="4000"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 系统配置 -->
        <el-card class="settings-card">
          <template #header>
            <span>系统配置</span>
          </template>
          
          <el-form :model="systemSettings" label-width="120px">
            <el-form-item label="任务超时时间">
              <el-input-number
                v-model="systemSettings.taskTimeout"
                :min="60"
                :max="3600"
                :step="60"
                style="width: 100%"
              />
              <div class="form-tip">
                任务执行超时时间（秒）
              </div>
            </el-form-item>

            <el-form-item label="最大重试次数">
              <el-input-number
                v-model="systemSettings.maxRetries"
                :min="0"
                :max="5"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="并发任务数">
              <el-input-number
                v-model="systemSettings.maxConcurrentTasks"
                :min="1"
                :max="10"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="日志级别">
              <el-select v-model="systemSettings.logLevel" style="width: 100%">
                <el-option label="DEBUG" value="DEBUG" />
                <el-option label="INFO" value="INFO" />
                <el-option label="WARN" value="WARN" />
                <el-option label="ERROR" value="ERROR" />
              </el-select>
            </el-form-item>

            <el-form-item label="自动清理">
              <el-switch v-model="systemSettings.autoCleanup" />
              <div class="form-tip">
                自动清理过期的任务和日志
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 操作按钮 -->
        <div class="settings-actions">
          <el-button @click="resetSettings">重置设置</el-button>
          <el-button type="primary" @click="saveSettings" :loading="saving">
            保存设置
          </el-button>
          <el-button type="success" @click="testConnection" :loading="testing">
            测试连接
          </el-button>
        </div>
      </el-col>

      <!-- 右侧信息面板 -->
      <el-col :span="8">
        <!-- 系统信息 -->
        <el-card class="info-card">
          <template #header>
            <span>系统信息</span>
          </template>
          
          <div class="system-info">
            <div class="info-item">
              <span class="label">版本号:</span>
              <span class="value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="label">构建时间:</span>
              <span class="value">2024-01-01</span>
            </div>
            <div class="info-item">
              <span class="label">运行时间:</span>
              <span class="value">{{ uptime }}</span>
            </div>
            <div class="info-item">
              <span class="label">内存使用:</span>
              <span class="value">{{ memoryUsage }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快速操作 -->
        <el-card class="info-card">
          <template #header>
            <span>快速操作</span>
          </template>
          
          <div class="quick-actions">
            <el-button type="primary" @click="exportSettings" style="width: 100%; margin-bottom: 12px;">
              <el-icon><Download /></el-icon>
              导出设置
            </el-button>
            <el-button @click="importSettings" style="width: 100%; margin-bottom: 12px;">
              <el-icon><Upload /></el-icon>
              导入设置
            </el-button>
            <el-button type="warning" @click="clearCache" style="width: 100%; margin-bottom: 12px;">
              <el-icon><Delete /></el-icon>
              清理缓存
            </el-button>
            <el-button type="danger" @click="resetSystem" style="width: 100%;">
              <el-icon><RefreshRight /></el-icon>
              重置系统
            </el-button>
          </div>
        </el-card>

        <!-- 帮助信息 -->
        <el-card class="info-card">
          <template #header>
            <span>帮助信息</span>
          </template>
          
          <div class="help-content">
            <el-alert
              title="配置说明"
              type="info"
              :closable="false"
              show-icon
            >
              <p>1. 请确保API密钥有效且有足够额度</p>
              <p>2. 建议使用qwen-max模型获得最佳效果</p>
              <p>3. 温度参数建议设置在0.7-0.9之间</p>
              <p>4. 定期清理缓存可提高系统性能</p>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const saving = ref(false)
const testing = ref(false)

// AI设置
const aiSettings = reactive({
  apiKey: '',
  chatModel: 'qwen-max',
  embeddingModel: 'text-embedding-v3',
  temperature: 0.7,
  maxTokens: 2000
})

// 系统设置
const systemSettings = reactive({
  taskTimeout: 1800,
  maxRetries: 3,
  maxConcurrentTasks: 5,
  logLevel: 'INFO',
  autoCleanup: true
})

// 系统信息
const uptime = ref('0天 0小时 0分钟')
const memoryUsage = ref('0 MB')

// 方法
const saveSettings = async () => {
  try {
    saving.value = true
    
    // 这里应该调用保存设置的API
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟API调用
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('保存设置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

const resetSettings = () => {
  // 重置AI设置
  aiSettings.apiKey = ''
  aiSettings.chatModel = 'qwen-max'
  aiSettings.embeddingModel = 'text-embedding-v3'
  aiSettings.temperature = 0.7
  aiSettings.maxTokens = 2000
  
  // 重置系统设置
  systemSettings.taskTimeout = 1800
  systemSettings.maxRetries = 3
  systemSettings.maxConcurrentTasks = 5
  systemSettings.logLevel = 'INFO'
  systemSettings.autoCleanup = true
  
  ElMessage.success('设置已重置')
}

const testConnection = async () => {
  try {
    testing.value = true
    
    // 这里应该调用测试连接的API
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟API调用
    
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error('连接测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

const exportSettings = () => {
  const settings = {
    ai: aiSettings,
    system: systemSettings
  }
  
  const blob = new Blob([JSON.stringify(settings, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'math-agent-settings.json'
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('设置导出成功')
}

const importSettings = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const settings = JSON.parse(e.target.result)
          Object.assign(aiSettings, settings.ai)
          Object.assign(systemSettings, settings.system)
          ElMessage.success('设置导入成功')
        } catch (error) {
          ElMessage.error('设置文件格式错误')
        }
      }
      reader.readAsText(file)
    }
  }
  input.click()
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理缓存吗？这将删除所有临时数据。',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用清理缓存的API
    ElMessage.success('缓存清理成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理缓存失败: ' + error.message)
    }
  }
}

const resetSystem = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置系统吗？这将删除所有数据并恢复默认设置。',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 这里应该调用重置系统的API
    ElMessage.success('系统重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('系统重置失败: ' + error.message)
    }
  }
}

// 生命周期
onMounted(() => {
  // 模拟加载系统信息
  uptime.value = '1天 2小时 30分钟'
  memoryUsage.value = '256 MB'
})
</script>

<style lang="scss" scoped>
.settings-view {
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

  .settings-card {
    margin-bottom: 20px;
    
    .form-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .settings-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }

  .info-card {
    margin-bottom: 20px;
    
    .system-info {
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .label {
          color: #606266;
          font-weight: 500;
        }
        
        .value {
          color: #303133;
          font-weight: 600;
        }
      }
    }
    
    .quick-actions {
      .el-button {
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
    
    .help-content {
      p {
        margin: 8px 0;
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .settings-view {
    .el-col {
      &:first-child {
        margin-bottom: 20px;
      }
    }
  }
}
</style>
