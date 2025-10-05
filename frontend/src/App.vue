<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">数学建模DeepResearch Agent</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="startNewChat" class="new-chat-btn">
              <el-icon><ChatDotRound /></el-icon>
              新建对话
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-container>
        <!-- 主内容区 - 全屏 -->
        <el-main class="app-main full-width">
          <router-view />
        </el-main>
      </el-container>
    </el-container>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()


// 方法
const startNewChat = async () => {
  try {
    const response = await fetch('/api/chat/sessions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: `对话 ${new Date().toLocaleString()}` })
    })
    const session = await response.json()
    router.push(`/chat/${session.id}`)
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

</script>

<style lang="scss" scoped>
.app-container {
  height: 100vh;
  background: #f8f9fa;
}

.app-header {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    padding: 0 24px;

    .logo {
      display: flex;
      align-items: center;
      font-size: 18px;
      font-weight: 500;

      .logo-icon {
        margin-right: 12px;
        font-size: 20px;
        color: #4285f4;
      }

    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;

      .new-chat-btn {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        
        &:hover {
          background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
        }
      }

    }
  }
}

.app-main {
  background: #f8f9fa;
  padding: 0;
  overflow-y: auto;
  
  &.full-width {
    width: 100%;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    
    .logo-text {
      font-size: 14px;
    }
  }
}
</style>