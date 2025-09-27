<template>
  <div class="home-view">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎使用 DeepResearchAgent</h1>
          <p class="subtitle">数学建模深度研究助手</p>
          <p class="description">
            基于AI的智能数学建模平台，自动完成从问题分析到论文生成的完整流程，
            让数学建模比赛变得更加高效和智能。
          </p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" size="large" @click="goToCreate">
            <el-icon><Plus /></el-icon>
            创建新任务
          </el-button>
          <el-button size="large" @click="goToTasks">
            <el-icon><List /></el-icon>
            查看任务列表
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 功能特性 -->
    <div class="features-section">
      <h2>核心功能</h2>
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" v-for="feature in features" :key="feature.title">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-content">
              <div class="feature-icon">
                <el-icon :size="32" :color="feature.color">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速开始 -->
    <div class="quick-start-section">
      <h2>快速开始</h2>
      <el-card class="quick-start-card">
        <el-steps :active="currentStep" finish-status="success">
          <el-step title="输入问题" description="描述你的数学建模问题" />
          <el-step title="自动分析" description="AI分析问题并制定解决方案" />
          <el-step title="生成代码" description="自动生成Python代码" />
          <el-step title="执行分析" description="运行代码并生成结果" />
          <el-step title="撰写论文" description="自动生成完整论文" />
        </el-steps>
        
        <div class="step-content">
          <el-button type="primary" @click="startDemo">
            <el-icon><PlayArrowFilled /></el-icon>
            开始体验
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <h2>平台统计</h2>
      <el-row :gutter="24">
        <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.label">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentStep = ref(0)

const features = [
  {
    title: '智能问题分析',
    description: '自动解析数学建模题目，识别问题类型和约束条件',
    icon: 'DataAnalysis',
    color: '#409EFF'
  },
  {
    title: '代码自动生成',
    description: '基于问题分析自动生成Python代码，支持多种算法',
    icon: 'Code',
    color: '#67C23A'
  },
  {
    title: '数据可视化',
    description: '自动生成图表和可视化结果，直观展示分析过程',
    icon: 'TrendCharts',
    color: '#E6A23C'
  },
  {
    title: '论文自动撰写',
    description: '生成结构完整的数学建模论文，符合学术规范',
    icon: 'Document',
    color: '#F56C6C'
  },
  {
    title: '实时进度跟踪',
    description: 'WebSocket实时显示研究进度，随时了解处理状态',
    icon: 'Timer',
    color: '#909399'
  },
  {
    title: '模块化设计',
    description: '各Agent独立工作，易于扩展和维护',
    icon: 'Grid',
    color: '#9C27B0'
  }
]

const stats = [
  { label: '总任务数', value: '128' },
  { label: '已完成', value: '95' },
  { label: '成功率', value: '94.2%' },
  { label: '平均用时', value: '15分钟' }
]

const goToCreate = () => {
  router.push('/create')
}

const goToTasks = () => {
  router.push('/tasks')
}

const startDemo = () => {
  // 模拟步骤进度
  const interval = setInterval(() => {
    currentStep.value++
    if (currentStep.value >= 5) {
      clearInterval(interval)
      currentStep.value = 0
    }
  }, 1000)
}
</script>

<style lang="scss" scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 40px;
  
  .welcome-content {
    display: flex;
    align-items: center;
    gap: 40px;
    
    .welcome-text {
      flex: 1;
      
      h1 {
        font-size: 32px;
        font-weight: bold;
        margin: 0 0 8px 0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .subtitle {
        font-size: 18px;
        color: #666;
        margin: 0 0 16px 0;
      }
      
      .description {
        font-size: 16px;
        color: #666;
        line-height: 1.6;
        margin: 0;
      }
    }
    
    .welcome-actions {
      display: flex;
      gap: 16px;
    }
  }
}

.features-section {
  margin-bottom: 40px;
  
  h2 {
    text-align: center;
    font-size: 28px;
    margin-bottom: 32px;
    color: #333;
  }
  
  .feature-card {
    height: 200px;
    margin-bottom: 24px;
    
    .feature-content {
      text-align: center;
      padding: 20px;
      
      .feature-icon {
        margin-bottom: 16px;
      }
      
      h3 {
        font-size: 18px;
        margin: 0 0 12px 0;
        color: #333;
      }
      
      p {
        font-size: 14px;
        color: #666;
        line-height: 1.5;
        margin: 0;
      }
    }
  }
}

.quick-start-section {
  margin-bottom: 40px;
  
  h2 {
    text-align: center;
    font-size: 28px;
    margin-bottom: 32px;
    color: #333;
  }
  
  .quick-start-card {
    .step-content {
      text-align: center;
      margin-top: 32px;
    }
  }
}

.stats-section {
  h2 {
    text-align: center;
    font-size: 28px;
    margin-bottom: 32px;
    color: #333;
  }
  
  .stat-card {
    text-align: center;
    
    .stat-content {
      padding: 20px;
      
      .stat-number {
        font-size: 32px;
        font-weight: bold;
        color: #409EFF;
        margin-bottom: 8px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #666;
      }
    }
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
    
    .welcome-actions {
      justify-content: center;
    }
  }
}
</style>
