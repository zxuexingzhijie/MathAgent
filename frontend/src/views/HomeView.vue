<template>
  <div class="home-view">
    <div class="welcome-section">
      <h1>数学建模Agent</h1>
      <p>AI协同工作，提供完整的数学建模解决方案</p>
    </div>

    <div class="quick-actions">
      <el-card class="action-card" @click="goToCreate">
        <div class="card-content">
          <el-icon class="card-icon"><Plus /></el-icon>
          <h3>创建任务</h3>
          <p>开始新的数学建模任务</p>
        </div>
      </el-card>

      <el-card class="action-card" @click="goToTasks">
        <div class="card-content">
          <el-icon class="card-icon"><List /></el-icon>
          <h3>任务管理</h3>
          <p>查看和管理现有任务</p>
        </div>
      </el-card>

    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToCreate = () => {
  router.push('/create')
}

const goToTasks = () => {
  router.push('/tasks')
}

</script>

<style lang="scss" scoped>
.home-view {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;

  .welcome-section {
    text-align: center;
    margin-bottom: 60px;

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

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;

    .action-card {
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid #e9ecef;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .card-content {
        text-align: center;
        padding: 32px 24px;

        .card-icon {
          font-size: 48px;
          color: #4285f4;
          margin-bottom: 16px;
        }

        h3 {
          font-size: 20px;
          color: #202124;
          margin: 0 0 8px 0;
          font-weight: 600;
        }

        p {
          font-size: 14px;
          color: #5f6368;
          margin: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .home-view {
    padding: 20px;

    .welcome-section {
      margin-bottom: 40px;

      h1 {
        font-size: 28px;
      }

      p {
        font-size: 16px;
      }
    }

    .quick-actions {
      grid-template-columns: 1fr;
      gap: 16px;
    }
  }
}
</style>