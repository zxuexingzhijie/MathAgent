<template>
  <div class="settings-panel">
    <el-form :model="settings" label-width="120px">
      <el-form-item label="OpenAI API Key">
        <el-input
          v-model="settings.openai_api_key"
          type="password"
          placeholder="请输入OpenAI API Key"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="Anthropic API Key">
        <el-input
          v-model="settings.anthropic_api_key"
          type="password"
          placeholder="请输入Anthropic API Key"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="默认模型">
        <el-select v-model="settings.default_model" placeholder="选择默认模型">
          <el-option label="GPT-4" value="gpt-4" />
          <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
          <el-option label="Claude-3 Sonnet" value="claude-3-sonnet-20240229" />
          <el-option label="Claude-3 Haiku" value="claude-3-haiku-20240307" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="代码模型">
        <el-select v-model="settings.code_model" placeholder="选择代码生成模型">
          <el-option label="GPT-4" value="gpt-4" />
          <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
          <el-option label="Claude-3 Sonnet" value="claude-3-sonnet-20240229" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="论文模型">
        <el-select v-model="settings.writing_model" placeholder="选择论文撰写模型">
          <el-option label="GPT-4" value="gpt-4" />
          <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
          <el-option label="Claude-3 Sonnet" value="claude-3-sonnet-20240229" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="任务超时时间">
        <el-input-number
          v-model="settings.task_timeout"
          :min="300"
          :max="7200"
          :step="300"
        />
        <span style="margin-left: 8px; color: #666;">秒</span>
      </el-form-item>
      
      <el-form-item label="最大并发任务">
        <el-input-number
          v-model="settings.max_concurrent_tasks"
          :min="1"
          :max="10"
        />
      </el-form-item>
      
      <el-form-item>
        <div class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存设置
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['close'])

const saving = ref(false)

// 设置数据
const settings = reactive({
  openai_api_key: '',
  anthropic_api_key: '',
  default_model: 'gpt-4',
  code_model: 'gpt-4',
  writing_model: 'gpt-4',
  task_timeout: 3600,
  max_concurrent_tasks: 5
})

// 加载设置
const loadSettings = () => {
  const savedSettings = localStorage.getItem('deepresearch_settings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      Object.assign(settings, parsed)
    } catch (error) {
      console.error('加载设置失败:', error)
    }
  }
}

// 保存设置
const handleSave = async () => {
  saving.value = true
  
  try {
    // 保存到本地存储
    localStorage.setItem('deepresearch_settings', JSON.stringify(settings))
    
    // 这里可以调用API保存到服务器
    // await settingsApi.saveSettings(settings)
    
    ElMessage.success('设置保存成功')
    emit('close')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('close')
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
.settings-panel {
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
