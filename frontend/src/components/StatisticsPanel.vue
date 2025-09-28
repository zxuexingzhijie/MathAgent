<template>
  <div class="statistics-panel">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="stat-card">
          <template #header>
            <span>任务统计</span>
          </template>
          <div class="stat-content">
            <div class="stat-item">
              <span class="label">总任务数:</span>
              <span class="value">{{ taskStore.taskCount }}</span>
            </div>
            <div class="stat-item">
              <span class="label">已完成:</span>
              <span class="value success">{{ taskStore.completedTasks.length }}</span>
            </div>
            <div class="stat-item">
              <span class="label">执行中:</span>
              <span class="value warning">{{ taskStore.runningTasks.length }}</span>
            </div>
            <div class="stat-item">
              <span class="label">失败:</span>
              <span class="value danger">{{ taskStore.failedTasks.length }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="stat-card">
          <template #header>
            <span>成功率</span>
          </template>
          <div class="success-rate">
            <el-progress
              :percentage="successRate"
              :color="getProgressColor(successRate)"
              :stroke-width="20"
              text-inside
            />
            <div class="rate-text">
              成功率: {{ successRate.toFixed(1) }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务类型分布 -->
    <el-card class="chart-card">
      <template #header>
        <span>任务类型分布</span>
      </template>
      <div class="chart-container">
        <v-chart
          :option="typeChartOption"
          style="height: 300px;"
        />
      </div>
    </el-card>

    <!-- 任务状态趋势 -->
    <el-card class="chart-card">
      <template #header>
        <span>任务状态趋势</span>
      </template>
      <div class="chart-container">
        <v-chart
          :option="statusChartOption"
          style="height: 300px;"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { useTaskStore } from '@/stores/taskStore'

use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const taskStore = useTaskStore()

// 计算属性
const successRate = computed(() => {
  const total = taskStore.taskCount
  const completed = taskStore.completedTasks.length
  return total > 0 ? (completed / total) * 100 : 0
})

const typeChartOption = computed(() => {
  const typeCount = {}
  taskStore.tasks.forEach(task => {
    typeCount[task.type] = (typeCount[task.type] || 0) + 1
  })

  const data = Object.entries(typeCount).map(([type, count]) => ({
    name: getTypeLabel(type),
    value: count
  }))

  return {
    title: {
      text: '任务类型分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '任务类型',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

const statusChartOption = computed(() => {
  const statusCount = {}
  taskStore.tasks.forEach(task => {
    statusCount[task.status] = (statusCount[task.status] || 0) + 1
  })

  const categories = Object.keys(statusCount)
  const data = Object.values(statusCount)

  return {
    title: {
      text: '任务状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: categories.map(status => getStatusLabel(status))
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '任务数量',
        type: 'line',
        data: data,
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
})

// 工具方法
const getTypeLabel = (type) => {
  const labelMap = {
    'OPTIMIZATION': '优化问题',
    'PREDICTION': '预测问题',
    'CLASSIFICATION': '分类问题',
    'SIMULATION': '仿真问题',
    'STATISTICAL_ANALYSIS': '统计分析',
    'MACHINE_LEARNING': '机器学习',
    'COMPLEX_SYSTEM': '复杂系统',
    'OTHER': '其他'
  }
  return labelMap[type] || type
}

const getStatusLabel = (status) => {
  const labelMap = {
    'CREATED': '已创建',
    'ANALYZING': '分析中',
    'DATA_COLLECTING': '数据收集中',
    'MODEL_BUILDING': '模型构建中',
    'SOLVING': '求解中',
    'GENERATING_REPORT': '生成报告中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return labelMap[status] || status
}

const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style lang="scss" scoped>
.statistics-panel {
  .stat-card {
    margin-bottom: 20px;
    
    .stat-content {
      .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .label {
          color: #606266;
          font-weight: 500;
        }
        
        .value {
          font-size: 18px;
          font-weight: bold;
          
          &.success {
            color: #67c23a;
          }
          
          &.warning {
            color: #e6a23c;
          }
          
          &.danger {
            color: #f56c6c;
          }
        }
      }
    }
    
    .success-rate {
      text-align: center;
      
      .rate-text {
        margin-top: 16px;
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .chart-card {
    margin-bottom: 20px;
    
    .chart-container {
      width: 100%;
    }
  }
}
</style>
