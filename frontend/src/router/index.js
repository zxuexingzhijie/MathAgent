import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'DeepResearch',
    component: () => import('@/views/DeepResearchView.vue'),
    meta: { title: '深度研究' }
  },
  {
    path: '/research/:id',
    name: 'ResearchDetail',
    component: () => import('@/views/DeepResearchView.vue'),
    meta: { title: '研究详情' }
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '任务管理' }
  },
  {
    path: '/create',
    name: 'CreateTask',
    component: () => import('@/views/CreateTaskView.vue'),
    meta: { title: '创建任务' }
  },
  {
    path: '/task/:id',
    name: 'TaskDetail',
    component: () => import('@/views/TaskDetailView.vue'),
    meta: { title: '任务详情' }
  },
  {
    path: '/results',
    name: 'Results',
    component: () => import('@/views/ResultsView.vue'),
    meta: { title: '结果查看' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { title: '系统设置' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面未找到' }
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
    document.title = `${to.meta.title} - 数学建模DeepResearch Agent`
  }
  next()
})

export default router