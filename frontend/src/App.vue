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
            <el-button type="primary" @click="goToCreate" class="new-task-btn">
              <el-icon><Plus /></el-icon>
              创建任务
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-container>
        <!-- 左侧边栏 - 简化版 -->
        <el-aside width="200px" class="app-sidebar">
          <!-- 导航菜单 -->
          <div class="nav-section">
            <div class="nav-item" @click="goToTasks">
              <el-icon><List /></el-icon>
              <span>任务管理</span>
            </div>
            <div class="nav-item" @click="goToCreate">
              <el-icon><Plus /></el-icon>
              <span>创建任务</span>
            </div>
          </div>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="app-main">
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
const goToTasks = () => {
  router.push('/tasks')
}

const goToCreate = () => {
  router.push('/create')
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

      .new-task-btn {
        background: #4285f4;
        border-color: #4285f4;
        
        &:hover {
          background: #3367d6;
          border-color: #3367d6;
        }
      }

    }
  }
}

.app-sidebar {
  background: white;
  border-right: 1px solid #e9ecef;
  padding: 16px 0;

  .nav-section {
    padding: 0 16px;

    .nav-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      border-radius: 8px;
      cursor: pointer;
      color: #202124;
      font-size: 14px;
      margin-bottom: 4px;
      transition: all 0.2s ease;

      &:hover {
        background: #f1f3f4;
      }

      .el-icon {
        margin-right: 12px;
        font-size: 16px;
        color: #5f6368;
      }
    }
  }
}

.app-main {
  background: #f8f9fa;
  padding: 0;
  overflow-y: auto;
}

// 响应式设计
@media (max-width: 768px) {
  .app-sidebar {
    width: 160px !important;
  }
  
  .header-content {
    padding: 0 16px;
    
    .logo-text {
      display: none;
    }
  }
}
</style>