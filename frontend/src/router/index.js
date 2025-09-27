import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('../views/TasksView.vue'),
    meta: {
      title: '任务列表'
    }
  },
  {
    path: '/task/:id',
    name: 'TaskDetail',
    component: () => import('../views/TaskDetailView.vue'),
    meta: {
      title: '任务详情'
    }
  },
  {
    path: '/create',
    name: 'CreateTask',
    component: () => import('../views/CreateTaskView.vue'),
    meta: {
      title: '创建任务'
    }
  },
  {
    path: '/results/:taskId',
    name: 'Results',
    component: () => import('../views/ResultsView.vue'),
    meta: {
      title: '结果查看'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - DeepResearchAgent`
  }
  next()
})

export default router
