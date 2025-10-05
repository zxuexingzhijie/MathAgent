<template>
  <div class="home-view">
    <div class="welcome-section">
      <h1>💬 对话式数学建模Agent</h1>
      <p>通过自然对话，完成数学建模、代码执行和论文撰写</p>
    </div>

    <!-- 快速开始 -->
    <div class="quick-start">
      <el-button type="primary" size="large" @click="startNewChat">
        <el-icon><ChatDotRound /></el-icon>
        开始新对话
      </el-button>
    </div>

    <!-- 历史会话 -->
    <div class="sessions-section">
      <div class="section-header">
        <h2>历史对话</h2>
        <el-button text @click="loadSessions">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="sessions.length === 0" class="empty-sessions">
        <el-empty description="暂无对话记录">
          <el-button type="primary" @click="startNewChat">开始第一个对话</el-button>
        </el-empty>
      </div>

      <div v-else class="sessions-grid">
        <el-card
          v-for="session in sessions"
          :key="session.id"
          class="session-card"
          @click="openSession(session.id)"
        >
          <div class="session-content">
            <div class="session-header">
              <h3>{{ session.title }}</h3>
              <el-dropdown @command="handleSessionCommand">
                <el-icon class="more-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'delete', id: session.id }">
                      删除对话
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="session-meta">
              <el-tag size="small" :type="getStatusType(session.status)">
                {{ getStatusLabel(session.status) }}
              </el-tag>
              <span class="session-time">{{ formatTime(session.updatedAt) }}</span>
              <span class="message-count">{{ session.messageCount }} 条消息</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../services/api'

const router = useRouter()

const sessions = ref([])
const loading = ref(false)

onMounted(() => {
  loadSessions()
})

const loadSessions = async () => {
  loading.value = true
  try {
    const response = await api.get('/chat/sessions')
    sessions.value = response.data
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    loading.value = false
  }
}

const startNewChat = async () => {
  try {
    const response = await api.post('/chat/sessions', {
      title: `对话 ${new Date().toLocaleString()}`
    })
    const session = response.data
    router.push(`/chat/${session.id}`)
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  }
}

const openSession = (sessionId) => {
  router.push(`/chat/${sessionId}`)
}

const handleSessionCommand = async (command) => {
  if (command.action === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await api.delete(`/chat/sessions/${command.id}`)
      ElMessage.success('删除成功')
      loadSessions()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除会话失败:', error)
        ElMessage.error('删除会话失败')
      }
    }
  }
}

const getStatusType = (status) => {
  const map = {
    'ACTIVE': 'success',
    'PROCESSING': 'warning',
    'COMPLETED': 'info',
    'ERROR': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'ACTIVE': '活跃',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'ERROR': '错误'
  }
  return map[status] || status
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

</script>

<style lang="scss" scoped>
.home-view {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;

  .welcome-section {
    text-align: center;
    margin-bottom: 40px;

    h1 {
      font-size: 36px;
      color: #202124;
      margin-bottom: 16px;
      font-weight: 600;
    }

    p {
      font-size: 18px;
      color: #5f6368;
      margin: 0;
    }
  }

  .quick-start {
    text-align: center;
    margin-bottom: 60px;
  }

  .sessions-section {
    margin-bottom: 60px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      h2 {
        font-size: 24px;
        color: #202124;
        margin: 0;
      }
    }

    .sessions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;

      .session-card {
        cursor: pointer;
        transition: all 0.3s ease;
        border: 1px solid #e9ecef;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }

        .session-content {
          .session-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 12px;

            h3 {
              font-size: 16px;
              color: #202124;
              margin: 0;
              font-weight: 500;
              flex: 1;
            }

            .more-icon {
              cursor: pointer;
              color: #909399;

              &:hover {
                color: #606266;
              }
            }
          }

          .session-meta {
            display: flex;
            gap: 12px;
            align-items: center;
            font-size: 12px;
            color: #909399;

            .session-time {
              flex: 1;
            }
          }
        }
      }
    }

    .empty-sessions {
      text-align: center;
      padding: 60px 0;
    }
  }
}

@media (max-width: 768px) {
  .home-view {
    padding: 20px;

    .welcome-section {
      margin-bottom: 30px;

      h1 {
        font-size: 28px;
      }

      p {
        font-size: 16px;
      }
    }

    .sessions-section {
      .sessions-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>