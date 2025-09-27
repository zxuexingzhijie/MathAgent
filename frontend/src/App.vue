<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">DeepResearchAgent</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="goToHome">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <el-button @click="goToTasks">
              <el-icon><List /></el-icon>
              任务列表
            </el-button>
            <el-button @click="showSettings">
              <el-icon><Setting /></el-icon>
              设置
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="app-main">
        <router-view />
      </el-main>

      <!-- 底部信息 -->
      <el-footer class="app-footer">
        <div class="footer-content">
          <span>© 2024 DeepResearchAgent - 数学建模深度研究助手</span>
        </div>
      </el-footer>
    </el-container>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="settingsVisible"
      title="系统设置"
      width="600px"
      :close-on-click-modal="false"
    >
      <SettingsPanel @close="settingsVisible = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import SettingsPanel from './components/SettingsPanel.vue'

const router = useRouter()
const settingsVisible = ref(false)

const goToHome = () => {
  router.push('/')
}

const goToTasks = () => {
  router.push('/tasks')
}

const showSettings = () => {
  settingsVisible.value = true
}
</script>

<style lang="scss" scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    padding: 0 24px;

    .logo {
      display: flex;
      align-items: center;
      font-size: 20px;
      font-weight: bold;

      .logo-icon {
        font-size: 24px;
        margin-right: 8px;
      }

      .logo-text {
        background: linear-gradient(45deg, #fff, #f0f0f0);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;

      .el-button {
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid rgba(255, 255, 255, 0.2);
        color: white;

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }
  }
}

.app-main {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 24px;
}

.app-footer {
  background: #2c3e50;
  color: white;
  text-align: center;
  height: 60px;
  line-height: 60px;

  .footer-content {
    font-size: 14px;
    opacity: 0.8;
  }
}
</style>
