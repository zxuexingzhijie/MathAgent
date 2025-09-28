<template>
  <div class="deep-research-view">
    <!-- 研究计划确认 -->
    <div v-if="!researchStarted" class="research-plan">
      <div class="plan-header">
        <h2>深度研究流程解析</h2>
        <p>这是我拟定的方案。如果你需要进行任何改动，请在我开始研究前告诉我。</p>
      </div>

      <div class="plan-steps">
        <!-- 研究网站 -->
        <div class="plan-step">
          <div class="step-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="step-content">
            <h3>研究网站</h3>
            <div class="step-items">
              <div class="step-item">
                <span class="step-number">(1)</span>
                <span>查找"深度研究"(Deep Research)这一概念的权威定义、核心目的以及其在商业、学术或技术领域中的普遍应用背景。</span>
              </div>
              <div class="step-item">
                <span class="step-number">(2)</span>
                <span>搜索主流研究机构、咨询公司或智库公开的深度研究(Deep Research)方法论和流程模型。</span>
              </div>
              <div class="step-item">
                <span class="step-number">(3)</span>
                <span>分析深度研究流程图或指南，识别其包含的<strong>主要阶段</strong>，通常包括：</span>
                <div class="sub-items">
                  <div class="sub-item">(a) 课题界定与研究假设确立</div>
                  <div class="sub-item">(b) 多源信息收集与筛选(包括一手和二手数据)</div>
                  <div class="sub-item">(c) 数据整合与交叉验证</div>
                  <div class="sub-item">(d) 深度分析与洞察提取</div>
                  <div class="sub-item">(e) 结论形成与报告撰写</div>
                </div>
              </div>
              <div class="more-link">
                <el-link type="primary">更多</el-link>
              </div>
            </div>
          </div>
        </div>

        <!-- 分析结果 -->
        <div class="plan-step">
          <div class="step-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="step-content">
            <h3>分析结果</h3>
            <div class="step-items">
              <div class="step-item">
                <span class="step-number">(1)</span>
                <span>建模手Agent将进行问题分析和数学建模</span>
              </div>
              <div class="step-item">
                <span class="step-number">(2)</span>
                <span>代码手Agent将生成和执行求解代码</span>
              </div>
              <div class="step-item">
                <span class="step-number">(3)</span>
                <span>论文手Agent将生成格式化的学术论文</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 生成报告 -->
        <div class="plan-step">
          <div class="step-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="step-content">
            <h3>生成报告</h3>
            <div class="step-items">
              <div class="step-item">
                <span class="step-number">(1)</span>
                <span>整合所有Agent的分析结果</span>
              </div>
              <div class="step-item">
                <span class="step-number">(2)</span>
                <span>生成完整的数学建模研究报告</span>
              </div>
              <div class="step-item">
                <span class="step-number">(3)</span>
                <span>提供可编辑的Jupyter Notebook</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="plan-footer">
        <p class="time-estimate">只需要几分钟就可以准备好</p>
        <div class="plan-actions">
          <el-button @click="modifyPlan">修改方案</el-button>
          <el-button type="primary" @click="startResearch">开始研究</el-button>
        </div>
      </div>
    </div>

    <!-- 研究进行中 -->
    <div v-else-if="researchInProgress" class="research-progress">
      <div class="progress-header">
        <h2>深度研究进行中</h2>
        <p>Multi-Agents正在协同工作，请稍候...</p>
      </div>

      <!-- Agent状态 -->
      <div class="agents-status">
        <div class="agent-card" :class="{ active: currentAgent === 'modeling' }">
          <div class="agent-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="agent-info">
            <h3>建模手</h3>
            <p>问题分析和数学建模</p>
            <el-progress :percentage="agentProgress.modeling" />
          </div>
        </div>

        <div class="agent-card" :class="{ active: currentAgent === 'coding' }">
          <div class="agent-icon">
            <el-icon><Code /></el-icon>
          </div>
          <div class="agent-info">
            <h3>代码手</h3>
            <p>代码生成和执行</p>
            <el-progress :percentage="agentProgress.coding" />
          </div>
        </div>

        <div class="agent-card" :class="{ active: currentAgent === 'writing' }">
          <div class="agent-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="agent-info">
            <h3>论文手</h3>
            <p>论文撰写和格式化</p>
            <el-progress :percentage="agentProgress.writing" />
          </div>
        </div>
      </div>

      <!-- 实时日志 -->
      <div class="research-logs">
        <h3>研究日志</h3>
        <div class="log-container">
          <div
            v-for="log in researchLogs"
            :key="log.id"
            class="log-item"
            :class="`log-${log.level}`"
          >
            <span class="log-time">{{ formatTime(log.time) }}</span>
            <span class="log-agent">[{{ log.agent }}]</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 研究完成 -->
    <div v-else-if="researchCompleted" class="research-results">
      <div class="results-header">
        <h2>深度研究完成</h2>
        <p>Multi-Agents已成功完成所有任务</p>
      </div>

      <!-- 结果展示 -->
      <div class="results-content">
        <!-- 建模结果 -->
        <div class="result-section">
          <h3>建模分析结果</h3>
          <div class="result-card">
            <pre>{{ researchResults.modeling }}</pre>
          </div>
        </div>

        <!-- 代码结果 -->
        <div class="result-section">
          <h3>代码执行结果</h3>
          <div class="result-card">
            <pre>{{ researchResults.coding }}</pre>
          </div>
        </div>

        <!-- 论文结果 -->
        <div class="result-section">
          <h3>生成论文</h3>
          <div class="result-card">
            <div class="paper-content" v-html="formattedPaper"></div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="results-actions">
        <el-button @click="downloadNotebook">下载Notebook</el-button>
        <el-button @click="downloadPaper">下载论文</el-button>
        <el-button type="primary" @click="startNewResearch">开始新研究</el-button>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-container">
        <el-input
          v-model="userInput"
          placeholder="问问 DeepResearch Agent"
          @keyup.enter="sendMessage"
          :disabled="researchInProgress"
        >
          <template #prepend>
            <el-icon><Plus /></el-icon>
          </template>
          <template #append>
            <div class="input-actions">
              <el-button
                v-for="action in inputActions"
                :key="action.name"
                :icon="action.icon"
                circle
                size="small"
                @click="action.handler"
              />
            </div>
          </template>
        </el-input>
      </div>
      <div class="disclaimer">
        DeepResearch Agent 的回答未必正确无误，请注意核查
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// 响应式数据
const researchStarted = ref(false)
const researchInProgress = ref(false)
const researchCompleted = ref(false)
const userInput = ref('')
const currentAgent = ref('modeling')

// Agent进度
const agentProgress = reactive({
  modeling: 0,
  coding: 0,
  writing: 0
})

// 研究日志
const researchLogs = ref([
  {
    id: 1,
    time: new Date(),
    agent: '建模手',
    level: 'info',
    message: '开始分析问题...'
  }
])

// 研究结果
const researchResults = reactive({
  modeling: '',
  coding: '',
  writing: ''
})

// 输入操作
const inputActions = [
  {
    name: 'Deep Research',
    icon: 'Star',
    handler: () => console.log('Deep Research')
  },
  {
    name: 'Image',
    icon: 'Picture',
    handler: () => console.log('Image')
  },
  {
    name: 'Canvas',
    icon: 'Grid',
    handler: () => console.log('Canvas')
  },
  {
    name: 'Learning',
    icon: 'Reading',
    handler: () => console.log('Learning')
  }
]

// 方法
const modifyPlan = () => {
  ElMessage.info('修改方案功能开发中...')
}

const startResearch = () => {
  researchStarted.value = true
  researchInProgress.value = true
  
  // 模拟研究过程
  simulateResearch()
}

const simulateResearch = () => {
  // 模拟建模手工作
  setTimeout(() => {
    currentAgent.value = 'modeling'
    agentProgress.modeling = 100
    addLog('建模手', 'info', '建模分析完成')
    
    // 模拟代码手工作
    setTimeout(() => {
      currentAgent.value = 'coding'
      agentProgress.coding = 100
      addLog('代码手', 'info', '代码生成和执行完成')
      
      // 模拟论文手工作
      setTimeout(() => {
        currentAgent.value = 'writing'
        agentProgress.writing = 100
        addLog('论文手', 'info', '论文生成完成')
        
        // 研究完成
        setTimeout(() => {
          researchInProgress.value = false
          researchCompleted.value = true
          loadResearchResults()
        }, 1000)
      }, 2000)
    }, 2000)
  }, 2000)
}

const addLog = (agent, level, message) => {
  researchLogs.value.push({
    id: Date.now(),
    time: new Date(),
    agent,
    level,
    message
  })
}

const loadResearchResults = () => {
  researchResults.modeling = `建模分析结果：
问题类型：优化问题
复杂度：中等
变量定义：x1, x2, x3
目标函数：max f(x) = 2x1 + 3x2 + x3
约束条件：
  x1 + x2 + x3 <= 100
  x1 >= 0, x2 >= 0, x3 >= 0`

  researchResults.coding = `代码执行结果：
求解方法：线性规划
最优解：x1=50, x2=30, x3=20
目标函数值：190
求解时间：0.05秒`

  researchResults.writing = `论文生成完成：
标题：基于线性规划的优化问题求解研究
摘要：本研究采用线性规划方法...
关键词：线性规划、优化、数学建模`
}

const downloadNotebook = () => {
  ElMessage.success('Notebook下载功能开发中...')
}

const downloadPaper = () => {
  ElMessage.success('论文下载功能开发中...')
}

const startNewResearch = () => {
  // 重置状态
  researchStarted.value = false
  researchInProgress.value = false
  researchCompleted.value = false
  currentAgent.value = 'modeling'
  
  Object.assign(agentProgress, {
    modeling: 0,
    coding: 0,
    writing: 0
  })
  
  researchLogs.value = []
}

const sendMessage = () => {
  if (!userInput.value.trim()) return
  
  addLog('用户', 'info', userInput.value)
  userInput.value = ''
}

const formatTime = (date) => {
  return dayjs(date).format('HH:mm:ss')
}

// 计算属性
const formattedPaper = computed(() => {
  return researchResults.writing.replace(/\n/g, '<br>')
})

// 生命周期
onMounted(() => {
  // 初始化
})

onUnmounted(() => {
  // 清理
})
</script>

<style lang="scss" scoped>
.deep-research-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;

  .research-plan {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .plan-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        margin: 0 0 8px 0;
        color: #202124;
        font-size: 24px;
        font-weight: 600;
      }

      p {
        margin: 0;
        color: #5f6368;
        font-size: 16px;
      }
    }

    .plan-steps {
      max-width: 800px;
      margin: 0 auto;

      .plan-step {
        display: flex;
        margin-bottom: 32px;
        padding: 24px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

        .step-icon {
          width: 48px;
          height: 48px;
          background: #4285f4;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20px;
          flex-shrink: 0;

          .el-icon {
            color: white;
            font-size: 20px;
          }
        }

        .step-content {
          flex: 1;

          h3 {
            margin: 0 0 16px 0;
            color: #202124;
            font-size: 18px;
            font-weight: 600;
          }

          .step-items {
            .step-item {
              margin-bottom: 12px;
              line-height: 1.6;

              .step-number {
                font-weight: 600;
                color: #4285f4;
                margin-right: 8px;
              }

              .sub-items {
                margin-left: 20px;
                margin-top: 8px;

                .sub-item {
                  margin-bottom: 4px;
                  color: #5f6368;
                }
              }
            }

            .more-link {
              margin-top: 12px;
            }
          }
        }
      }
    }

    .plan-footer {
      text-align: center;
      margin-top: 32px;

      .time-estimate {
        margin: 0 0 16px 0;
        color: #5f6368;
        font-size: 14px;
      }

      .plan-actions {
        display: flex;
        justify-content: center;
        gap: 16px;
      }
    }
  }

  .research-progress {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .progress-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        margin: 0 0 8px 0;
        color: #202124;
        font-size: 24px;
        font-weight: 600;
      }

      p {
        margin: 0;
        color: #5f6368;
        font-size: 16px;
      }
    }

    .agents-status {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 32px;

      .agent-card {
        padding: 20px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;

        &.active {
          border: 2px solid #4285f4;
          box-shadow: 0 4px 12px rgba(66, 133, 244, 0.2);
        }

        .agent-icon {
          width: 40px;
          height: 40px;
          background: #4285f4;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 12px;

          .el-icon {
            color: white;
            font-size: 18px;
          }
        }

        .agent-info {
          h3 {
            margin: 0 0 8px 0;
            color: #202124;
            font-size: 16px;
            font-weight: 600;
          }

          p {
            margin: 0 0 12px 0;
            color: #5f6368;
            font-size: 14px;
          }
        }
      }
    }

    .research-logs {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

      h3 {
        margin: 0 0 16px 0;
        color: #202124;
        font-size: 18px;
        font-weight: 600;
      }

      .log-container {
        max-height: 300px;
        overflow-y: auto;

        .log-item {
          padding: 8px 0;
          border-bottom: 1px solid #f1f3f4;
          font-size: 14px;

          &:last-child {
            border-bottom: none;
          }

          .log-time {
            color: #5f6368;
            margin-right: 8px;
            font-family: monospace;
          }

          .log-agent {
            color: #4285f4;
            margin-right: 8px;
            font-weight: 500;
          }

          .log-message {
            color: #202124;
          }

          &.log-info {
            .log-agent {
              color: #4285f4;
            }
          }

          &.log-success {
            .log-agent {
              color: #34a853;
            }
          }

          &.log-warning {
            .log-agent {
              color: #fbbc04;
            }
          }

          &.log-error {
            .log-agent {
              color: #ea4335;
            }
          }
        }
      }
    }
  }

  .research-results {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .results-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        margin: 0 0 8px 0;
        color: #202124;
        font-size: 24px;
        font-weight: 600;
      }

      p {
        margin: 0;
        color: #5f6368;
        font-size: 16px;
      }
    }

    .results-content {
      max-width: 1000px;
      margin: 0 auto;

      .result-section {
        margin-bottom: 32px;

        h3 {
          margin: 0 0 16px 0;
          color: #202124;
          font-size: 18px;
          font-weight: 600;
        }

        .result-card {
          background: white;
          border-radius: 12px;
          padding: 20px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

          pre {
            margin: 0;
            white-space: pre-wrap;
            font-family: 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.6;
          }

          .paper-content {
            line-height: 1.8;
            color: #202124;
          }
        }
      }
    }

    .results-actions {
      text-align: center;
      margin-top: 32px;

      .el-button {
        margin: 0 8px;
      }
    }
  }

  .input-area {
    padding: 16px 24px;
    background: white;
    border-top: 1px solid #e9ecef;

    .input-container {
      margin-bottom: 8px;

      .input-actions {
        display: flex;
        gap: 8px;
      }
    }

    .disclaimer {
      text-align: center;
      font-size: 12px;
      color: #5f6368;
    }
  }
}
</style>
