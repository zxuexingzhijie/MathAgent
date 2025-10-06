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
          <el-option label="中国数学建模竞赛" value="CHINA" />
          <el-option label="美国大学生数学建模竞赛" value="USA" />
          <el-option label="自定义模板" value="CUSTOM" />
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
