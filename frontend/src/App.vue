<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">数学建模DeepResearch Agent</span>
            <el-dropdown class="model-selector">
              <span class="model-text">
                Multi-Agents <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>建模手 + 代码手 + 论文手</el-dropdown-item>
                  <el-dropdown-item>建模手 + 代码手</el-dropdown-item>
                  <el-dropdown-item>仅建模手</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="startNewResearch" class="new-research-btn">
              <el-icon><Plus /></el-icon>
              发起新研究
            </el-button>
            <el-button @click="showSettings" class="settings-btn">
              <el-icon><Setting /></el-icon>
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-container>
        <!-- 左侧边栏 -->
        <el-aside width="280px" class="app-sidebar">
          <!-- Gem功能区域 -->
          <div class="gem-section">
            <div class="section-title">Gem</div>
            <div class="gem-item" @click="openStorybook">
              <el-icon><Document /></el-icon>
              <span>Storybook</span>
            </div>
            <div class="gem-item" @click="exploreGem">
              <el-icon><Star /></el-icon>
              <span>探索 Gem</span>
            </div>
          </div>

          <!-- 近期研究 -->
          <div class="recent-section">
            <div class="section-title">近期研究</div>
            <div class="research-list">
              <div
                v-for="research in recentResearches"
                :key="research.id"
                class="research-item"
                :class="{ active: currentResearchId === research.id }"
                @click="selectResearch(research)"
              >
                <div class="research-title">{{ research.title }}</div>
                <div class="research-time">{{ formatTime(research.createdAt) }}</div>
                <div v-if="research.hasNew" class="new-indicator"></div>
              </div>
            </div>
          </div>

          <!-- 底部设置 -->
          <div class="bottom-section">
            <div class="settings-help" @click="showSettings">
              <el-icon><Setting /></el-icon>
              <span>设置和帮助</span>
            </div>
          </div>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <!-- 新研究对话框 -->
    <el-dialog
      v-model="showNewResearchDialog"
      title="发起新的深度研究"
      width="800px"
      :before-close="handleCloseNewResearchDialog"
    >
      <NewResearchForm @research-created="handleResearchCreated" />
    </el-dialog>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="showSettingsDialog"
      title="系统设置"
      width="600px"
    >
      <SettingsPanel />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTaskStore } from '@/stores/taskStore'
import NewResearchForm from '@/components/NewResearchForm.vue'
import SettingsPanel from '@/components/SettingsPanel.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const taskStore = useTaskStore()

// 响应式数据
const showNewResearchDialog = ref(false)
const showSettingsDialog = ref(false)
const currentResearchId = ref(null)

// 模拟近期研究数据
const recentResearches = ref([
  {
    id: 1,
    title: "深度研究计划确认",
    createdAt: new Date(),
    hasNew: false
  },
  {
    id: 2,
    title: "Spring AI Alibaba问题分析",
    createdAt: new Date(Date.now() - 3600000),
    hasNew: true
  },
  {
    id: 3,
    title: "Spring AI依赖冲突问题",
    createdAt: new Date(Date.now() - 7200000),
    hasNew: false
  },
  {
    id: 4,
    title: "数据分析方案拟定",
    createdAt: new Date(Date.now() - 10800000),
    hasNew: false
  },
  {
    id: 5,
    title: "运输问题求解过程详解",
    createdAt: new Date(Date.now() - 14400000),
    hasNew: false
  }
])

// 计算属性
const currentRoute = computed(() => route.path)

// 方法
const startNewResearch = () => {
  showNewResearchDialog.value = true
}

const showSettings = () => {
  showSettingsDialog.value = true
}

const handleCloseNewResearchDialog = () => {
  showNewResearchDialog.value = false
}

const handleResearchCreated = (research) => {
  showNewResearchDialog.value = false
  recentResearches.value.unshift(research)
  router.push(`/research/${research.id}`)
}

const selectResearch = (research) => {
  currentResearchId.value = research.id
  router.push(`/research/${research.id}`)
}

const openStorybook = () => {
  // 打开Storybook
  console.log('打开Storybook')
}

const exploreGem = () => {
  // 探索Gem功能
  console.log('探索Gem')
}

const formatTime = (date) => {
  return dayjs(date).fromNow()
}

// 生命周期
onMounted(() => {
  taskStore.loadTasks()
})
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

      .model-selector {
        margin-left: 16px;
        cursor: pointer;
        
        .model-text {
          color: #5f6368;
          font-size: 14px;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;

      .new-research-btn {
        background: #4285f4;
        border-color: #4285f4;
        
        &:hover {
          background: #3367d6;
          border-color: #3367d6;
        }
      }

      .settings-btn {
        border: none;
        background: transparent;
        color: #5f6368;
        
        &:hover {
          background: #f1f3f4;
        }
      }
    }
  }
}

.app-sidebar {
  background: white;
  border-right: 1px solid #e9ecef;
  padding: 16px 0;

  .gem-section {
    padding: 0 16px 24px;
    border-bottom: 1px solid #e9ecef;
    margin-bottom: 16px;

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: #5f6368;
      margin-bottom: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .gem-item {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-radius: 8px;
      cursor: pointer;
      color: #202124;
      font-size: 14px;
      margin-bottom: 4px;

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

  .recent-section {
    padding: 0 16px;
    flex: 1;

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: #5f6368;
      margin-bottom: 12px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .research-list {
      .research-item {
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        margin-bottom: 4px;
        position: relative;
        transition: all 0.2s ease;

        &:hover {
          background: #f1f3f4;
        }

        &.active {
          background: #e8f0fe;
          border-left: 3px solid #4285f4;
        }

        .research-title {
          font-size: 14px;
          color: #202124;
          margin-bottom: 4px;
          line-height: 1.4;
        }

        .research-time {
          font-size: 12px;
          color: #5f6368;
        }

        .new-indicator {
          position: absolute;
          top: 12px;
          right: 12px;
          width: 8px;
          height: 8px;
          background: #4285f4;
          border-radius: 50%;
        }
      }
    }
  }

  .bottom-section {
    padding: 16px;
    border-top: 1px solid #e9ecef;
    margin-top: auto;

    .settings-help {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-radius: 8px;
      cursor: pointer;
      color: #5f6368;
      font-size: 14px;

      &:hover {
        background: #f1f3f4;
      }

      .el-icon {
        margin-right: 12px;
        font-size: 16px;
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
    width: 200px !important;
  }
  
  .header-content {
    padding: 0 16px;
    
    .logo-text {
      display: none;
    }
  }
}
</style>