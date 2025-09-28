<template>
  <div class="create-task-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="left"
    >
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

      <el-form-item label="问题陈述" prop="problemStatement">
        <el-input
          v-model="form.problemStatement"
          type="textarea"
          :rows="6"
          placeholder="请详细描述数学建模问题"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="研究目标" prop="researchGoals">
        <el-input
          v-model="form.researchGoals"
          type="textarea"
          :rows="3"
          placeholder="请描述研究的具体目标"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="数据需求" prop="dataRequirements">
        <el-input
          v-model="form.dataRequirements"
          type="textarea"
          :rows="3"
          placeholder="请描述需要的数据类型和来源"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="模型约束" prop="modelConstraints">
        <el-input
          v-model="form.modelConstraints"
          type="textarea"
          :rows="3"
          placeholder="请描述模型的约束条件"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="预期输出" prop="expectedOutputs">
        <el-input
          v-model="form.expectedOutputs"
          type="textarea"
          :rows="3"
          placeholder="请描述期望的输出格式"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            创建任务
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useTaskStore } from '@/stores/taskStore'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['task-created'])

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
    
    const taskData = { ...form }
    const newTask = await taskStore.createTask(taskData)
    
    ElMessage.success('任务创建成功！')
    emit('task-created', newTask)
    
  } catch (error) {
    if (error.message) {
      ElMessage.error('创建任务失败: ' + error.message)
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
</script>

<style lang="scss" scoped>
.create-task-form {
  .form-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 24px;
  }
}
</style>
