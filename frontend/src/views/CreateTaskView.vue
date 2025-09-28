<template>
  <div class="create-task-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>创建数学建模任务</h1>
      <p>输入问题描述，系统将自动分析并构建研究流程</p>
    </div>

    <!-- 创建表单 -->
    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="left"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3>基本信息</h3>
          
          <el-form-item label="任务标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入任务标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="任务类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择任务类型" style="width: 100%">
              <el-option
                v-for="type in taskTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="任务描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入任务描述"
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
              placeholder="请详细描述数学建模问题，包括：1. 问题背景和目标 2. 已知条件和约束 3. 需要求解的内容 4. 预期结果"
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

        <!-- 技术要求 -->
        <div class="form-section">
          <h3>技术要求</h3>
          
          <el-form-item label="数据需求" prop="dataRequirements">
            <el-input
              v-model="form.dataRequirements"
              type="textarea"
              :rows="3"
              placeholder="请描述需要的数据类型、来源和格式要求"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="模型约束" prop="modelConstraints">
            <el-input
              v-model="form.modelConstraints"
              type="textarea"
              :rows="3"
              placeholder="请描述模型的约束条件、限制和特殊要求"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="预期输出" prop="expectedOutputs">
            <el-input
              v-model="form.expectedOutputs"
              type="textarea"
              :rows="3"
              placeholder="请描述期望的输出格式、报告内容和交付物"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            创建任务
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 预览卡片 -->
    <el-card v-if="showPreview" class="preview-card">
      <template #header>
        <span>任务预览</span>
      </template>
      
      <div class="preview-content">
        <h4>{{ form.title }}</h4>
        <p><strong>类型:</strong> {{ getTypeLabel(form.type) }}</p>
        <p><strong>描述:</strong> {{ form.description }}</p>
        <div class="preview-section">
          <h5>问题陈述</h5>
          <p>{{ form.problemStatement }}</p>
        </div>
        <div class="preview-section">
          <h5>研究目标</h5>
          <p>{{ form.researchGoals }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const taskStore = useTaskStore()

// 表单引用
const formRef = ref()

// 响应式数据
const submitting = ref(false)
const showPreview = computed(() => form.title && form.problemStatement)

// 表单数据
const form = reactive({
  title: '',
  description: '',
  type: '',
  problemStatement: '',
  researchGoals: '',
  dataRequirements: '',
  modelConstraints: '',
  expectedOutputs: ''
})

// 任务类型选项
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

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度应在5-200个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ],
  problemStatement: [
    { required: true, message: '请输入问题陈述', trigger: 'blur' },
    { min: 50, max: 2000, message: '问题陈述长度应在50-2000个字符之间', trigger: 'blur' }
  ],
  researchGoals: [
    { max: 1000, message: '研究目标长度不能超过1000个字符', trigger: 'blur' }
  ],
  dataRequirements: [
    { max: 1000, message: '数据需求长度不能超过1000个字符', trigger: 'blur' }
  ],
  modelConstraints: [
    { max: 1000, message: '模型约束长度不能超过1000个字符', trigger: 'blur' }
  ],
  expectedOutputs: [
    { max: 1000, message: '预期输出长度不能超过1000个字符', trigger: 'blur' }
  ]
}

// 方法
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const taskData = {
      title: form.title,
      description: form.description,
      type: form.type,
      problemStatement: form.problemStatement,
      researchGoals: form.researchGoals,
      dataRequirements: form.dataRequirements,
      modelConstraints: form.modelConstraints,
      expectedOutputs: form.expectedOutputs
    }
    
    console.log('发送的任务数据:', taskData)
    const newTask = await taskStore.createTask(taskData)
    
    ElMessage.success('任务创建成功！')
    router.push(`/task/${newTask.id}`)
    
  } catch (error) {
    console.error('创建任务错误:', error)
    if (error.message) {
      ElMessage.error('创建任务失败: ' + error.message)
    } else {
      ElMessage.error('创建任务失败，请检查网络连接')
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const getTypeLabel = (type) => {
  const typeObj = taskTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}
</script>

<style lang="scss" scoped>
.create-task-view {
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

  .form-card {
    margin-bottom: 24px;
    
    .form-section {
      margin-bottom: 32px;
      
      h3 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
        border-bottom: 2px solid #409eff;
        padding-bottom: 8px;
      }
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

  .preview-card {
    .preview-content {
      h4 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 20px;
        font-weight: 600;
      }
      
      p {
        margin: 0 0 12px 0;
        color: #606266;
        line-height: 1.6;
      }
      
      .preview-section {
        margin-top: 20px;
        
        h5 {
          margin: 0 0 8px 0;
          color: #303133;
          font-size: 16px;
          font-weight: 600;
        }
        
        p {
          background: #f8f9fa;
          padding: 12px;
          border-radius: 6px;
          border-left: 4px solid #409eff;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .create-task-view {
    .form-card {
      .el-form {
        :deep(.el-form-item__label) {
          width: 100px !important;
        }
      }
    }
  }
}
</style>