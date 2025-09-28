<template>
  <div class="new-research-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="left"
    >
      <!-- 研究主题 -->
      <div class="form-section">
        <h3>研究主题</h3>
        
        <el-form-item label="研究标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入研究标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="研究类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择研究类型" style="width: 100%">
            <el-option
              v-for="type in researchTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="研究描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请描述研究背景和目标"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>

      <!-- 问题描述 -->
      <div class="form-section">
        <h3>问题描述</h3>
        
        <el-form-item label="问题陈述" prop="problemStatement">
          <el-input
            v-model="form.problemStatement"
            type="textarea"
            :rows="6"
            placeholder="请详细描述数学建模问题，包括：&#10;1. 问题背景和目标&#10;2. 已知条件和约束&#10;3. 需要求解的内容&#10;4. 预期结果"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="研究目标" prop="researchGoals">
          <el-input
            v-model="form.researchGoals"
            type="textarea"
            :rows="3"
            placeholder="请描述研究的具体目标和预期成果"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </div>

      <!-- Agent配置 -->
      <div class="form-section">
        <h3>Agent配置</h3>
        
        <el-form-item label="启用Agent">
          <el-checkbox-group v-model="form.enabledAgents">
            <el-checkbox label="modeling">建模手</el-checkbox>
            <el-checkbox label="coding">代码手</el-checkbox>
            <el-checkbox label="writing">论文手</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="模型配置">
          <el-radio-group v-model="form.modelConfig">
            <el-radio label="balanced">平衡模式</el-radio>
            <el-radio label="accuracy">精确模式</el-radio>
            <el-radio label="creative">创造模式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="代码执行">
          <el-switch v-model="form.enableCodeExecution" />
          <span class="form-tip">启用基于Jupyter的代码执行环境</span>
        </el-form-item>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          开始深度研究
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['research-created'])

const taskStore = useTaskStore()
const formRef = ref()
const submitting = ref(false)

// 表单数据
const form = reactive({
  title: '',
  description: '',
  type: '',
  problemStatement: '',
  researchGoals: '',
  enabledAgents: ['modeling', 'coding', 'writing'],
  modelConfig: 'balanced',
  enableCodeExecution: true
})

// 研究类型选项
const researchTypes = [
  { value: 'OPTIMIZATION', label: '优化问题研究' },
  { value: 'PREDICTION', label: '预测问题研究' },
  { value: 'CLASSIFICATION', label: '分类问题研究' },
  { value: 'SIMULATION', label: '仿真问题研究' },
  { value: 'STATISTICAL_ANALYSIS', label: '统计分析研究' },
  { value: 'MACHINE_LEARNING', label: '机器学习研究' },
  { value: 'COMPLEX_SYSTEM', label: '复杂系统研究' },
  { value: 'CUSTOM', label: '自定义研究' }
]

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入研究标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度应在5-200个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择研究类型', trigger: 'change' }
  ],
  problemStatement: [
    { required: true, message: '请输入问题陈述', trigger: 'blur' },
    { min: 50, max: 2000, message: '问题陈述长度应在50-2000个字符之间', trigger: 'blur' }
  ]
}

// 方法
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const researchData = {
      title: form.title,
      description: form.description,
      type: form.type,
      problemStatement: form.problemStatement,
      researchGoals: form.researchGoals,
      enabledAgents: form.enabledAgents,
      modelConfig: form.modelConfig,
      enableCodeExecution: form.enableCodeExecution
    }
    
    // 这里应该调用创建研究的API
    const newResearch = await createResearch(researchData)
    
    ElMessage.success('深度研究已开始！')
    emit('research-created', newResearch)
    
  } catch (error) {
    if (error.message) {
      ElMessage.error('创建研究失败: ' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

const createResearch = async (researchData) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        id: Date.now(),
        ...researchData,
        status: 'STARTED',
        createdAt: new Date()
      })
    }, 1000)
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style lang="scss" scoped>
.new-research-form {
  .form-section {
    margin-bottom: 32px;
    
    h3 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 18px;
      font-weight: 600;
      border-bottom: 2px solid #4285f4;
      padding-bottom: 8px;
    }
  }
  
  .form-tip {
    margin-left: 8px;
    font-size: 12px;
    color: #909399;
  }
  
  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }
}
</style>
