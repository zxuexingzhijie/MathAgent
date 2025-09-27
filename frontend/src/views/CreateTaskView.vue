<template>
  <div class="create-task-view">
    <el-card class="create-card">
      <template #header>
        <div class="card-header">
          <h2>创建新任务</h2>
          <p>输入数学建模问题，AI将自动分析并生成解决方案</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入任务标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="问题类型" prop="task_type">
          <el-select v-model="form.task_type" placeholder="请选择问题类型">
            <el-option label="数学建模" value="math_modeling" />
            <el-option label="数据分析" value="data_analysis" />
            <el-option label="优化问题" value="optimization" />
            <el-option label="仿真模拟" value="simulation" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述你的数学建模问题，包括背景、目标、数据等信息"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="约束条件">
          <el-input
            v-model="form.constraints"
            type="textarea"
            :rows="3"
            placeholder="请描述问题的约束条件（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="输入数据">
          <el-input
            v-model="form.input_data_text"
            type="textarea"
            :rows="4"
            placeholder="请提供相关数据或数据描述（可选）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="高级配置">
          <el-collapse>
            <el-collapse-item title="模型配置" name="model">
              <el-form-item label="分析模型">
                <el-select v-model="form.config.analysis_model" placeholder="选择分析模型">
                  <el-option label="GPT-4" value="gpt-4" />
                  <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
                  <el-option label="Claude-3" value="claude-3-sonnet-20240229" />
                </el-select>
              </el-form-item>
              <el-form-item label="代码模型">
                <el-select v-model="form.config.code_model" placeholder="选择代码模型">
                  <el-option label="GPT-4" value="gpt-4" />
                  <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
                  <el-option label="Claude-3" value="claude-3-sonnet-20240229" />
                </el-select>
              </el-form-item>
              <el-form-item label="论文模型">
                <el-select v-model="form.config.writing_model" placeholder="选择论文模型">
                  <el-option label="GPT-4" value="gpt-4" />
                  <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
                  <el-option label="Claude-3" value="claude-3-sonnet-20240229" />
                </el-select>
              </el-form-item>
            </el-collapse-item>
          </el-collapse>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              <el-icon><Plus /></el-icon>
              创建任务
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 示例问题 -->
    <el-card class="examples-card" v-if="!form.title">
      <template #header>
        <h3>示例问题</h3>
      </template>
      
      <div class="examples">
        <div 
          v-for="example in examples" 
          :key="example.title"
          class="example-item"
          @click="loadExample(example)"
        >
          <h4>{{ example.title }}</h4>
          <p>{{ example.description }}</p>
          <el-tag :type="getTaskTypeTag(example.task_type)">
            {{ getTaskTypeLabel(example.task_type) }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useTaskStore } from '../stores/taskStore'
import { ElMessage } from 'element-plus'

const router = useRouter()
const taskStore = useTaskStore()
const formRef = ref()
const loading = ref(false)

// 表单数据
const form = reactive({
  title: '',
  description: '',
  task_type: '',
  constraints: '',
  input_data_text: '',
  config: {
    analysis_model: 'gpt-4',
    code_model: 'gpt-4',
    writing_model: 'gpt-4'
  }
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 3, max: 100, message: '标题长度在3到100个字符', trigger: 'blur' }
  ],
  task_type: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度在10到2000个字符', trigger: 'blur' }
  ]
}

// 示例问题
const examples = [
  {
    title: '城市交通流量优化',
    description: '基于历史交通数据，建立数学模型优化城市交通信号灯控制策略，减少拥堵时间',
    task_type: 'optimization',
    constraints: '考虑道路容量限制、信号灯切换时间约束',
    input_data_text: '历史交通流量数据、道路网络拓扑、信号灯配置信息'
  },
  {
    title: '股票价格预测',
    description: '使用机器学习方法分析股票历史数据，预测未来价格走势',
    task_type: 'data_analysis',
    constraints: '考虑市场波动性、政策影响等因素',
    input_data_text: '股票历史价格、成交量、技术指标、宏观经济数据'
  },
  {
    title: '疫情传播模型',
    description: '建立传染病传播的数学模型，预测疫情发展趋势',
    task_type: 'math_modeling',
    constraints: '考虑人口流动、防控措施等因素',
    input_data_text: '人口数据、疫情统计数据、防控政策信息'
  }
]

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 准备提交数据
    const taskData = {
      title: form.title,
      description: form.description,
      task_type: form.task_type,
      constraints: form.constraints || null,
      input_data: form.input_data_text ? { text: form.input_data_text } : null,
      config: form.config
    }
    
    // 创建任务
    const task = await taskStore.createTask(taskData)
    
    ElMessage.success('任务创建成功！')
    
    // 跳转到任务详情页
    router.push(`/task/${task.id}`)
    
  } catch (error) {
    console.error('创建任务失败:', error)
    ElMessage.error('创建任务失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载示例
const loadExample = (example) => {
  form.title = example.title
  form.description = example.description
  form.task_type = example.task_type
  form.constraints = example.constraints
  form.input_data_text = example.input_data_text
}

// 返回上一页
const goBack = () => {
  router.back()
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
</script>

<style lang="scss" scoped>
.create-task-view {
  max-width: 800px;
  margin: 0 auto;
}

.create-card {
  margin-bottom: 24px;
  
  .card-header {
    h2 {
      margin: 0 0 8px 0;
      color: #333;
    }
    
    p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
  }
}

.examples-card {
  .examples {
    display: grid;
    gap: 16px;
    
    .example-item {
      padding: 16px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
        background-color: #f0f9ff;
      }
      
      h4 {
        margin: 0 0 8px 0;
        color: #333;
        font-size: 16px;
      }
      
      p {
        margin: 0 0 12px 0;
        color: #666;
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }
}
</style>
