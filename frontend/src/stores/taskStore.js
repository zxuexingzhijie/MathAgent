import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { taskApi } from '@/services/api'

export const useTaskStore = defineStore('task', () => {
  // 状态
  const tasks = ref([])
  const currentTask = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const taskCount = computed(() => tasks.value.length)
  const completedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'COMPLETED')
  )
  const runningTasks = computed(() => 
    tasks.value.filter(task => 
      ['ANALYZING', 'DATA_COLLECTING', 'MODEL_BUILDING', 'SOLVING', 'GENERATING_REPORT'].includes(task.status)
    )
  )
  const failedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'FAILED')
  )

  // 动作
  const loadTasks = async () => {
    console.log('taskStore: loadTasks called')
    loading.value = true
    error.value = null
    try {
      console.log('taskStore: calling taskApi.getTasks()')
      const response = await taskApi.getTasks()
      console.log('taskStore: received response:', response)
      tasks.value = response.data
      console.log('taskStore: tasks updated to:', tasks.value)
    } catch (err) {
      error.value = err.message
      console.error('加载任务失败:', err)
    } finally {
      loading.value = false
    }
  }

  // 添加fetchTasks方法以兼容TasksView.vue
  const fetchTasks = async (params = {}) => {
    console.log('fetchTasks called with params:', params)
    return await loadTasks()
  }

  // 添加deleteTask方法 - 后端未实现，暂时移除
  // const deleteTask = async (taskId) => {
  //   // 后端没有删除接口，暂时不实现
  //   throw new Error('删除功能暂未实现')
  // }

  const createTask = async (taskData) => {
    loading.value = true
    error.value = null
    try {
      const response = await taskApi.createTask(taskData)
      const newTask = response.data
      tasks.value.unshift(newTask)
      return newTask
    } catch (err) {
      error.value = err.message
      console.error('创建任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const executeTask = async (taskId) => {
    loading.value = true
    error.value = null
    try {
      const response = await taskApi.executeTask(taskId)
      // 更新任务状态
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.status = 'ANALYZING'
        task.startedAt = new Date().toISOString()
      }
      return response.data
    } catch (err) {
      error.value = err.message
      console.error('执行任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const cancelTask = async (taskId) => {
    loading.value = true
    error.value = null
    try {
      const response = await taskApi.cancelTask(taskId)
      // 更新任务状态
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.status = 'CANCELLED'
        task.completedAt = new Date().toISOString()
      }
      return response.data
    } catch (err) {
      error.value = err.message
      console.error('取消任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getTaskDetail = async (taskId) => {
    loading.value = true
    error.value = null
    try {
      const response = await taskApi.getTaskDetail(taskId)
      currentTask.value = response.data
      return response.data
    } catch (err) {
      error.value = err.message
      console.error('获取任务详情失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getTaskResults = async (taskId) => {
    try {
      const response = await taskApi.getTaskResults(taskId)
      return response.data
    } catch (err) {
      console.error('获取任务结果失败:', err)
      throw err
    }
  }

  const getTaskLogs = async (taskId) => {
    try {
      const response = await taskApi.getTaskLogs(taskId)
      return response.data
    } catch (err) {
      console.error('获取任务日志失败:', err)
      throw err
    }
  }

  const addTask = (task) => {
    tasks.value.unshift(task)
  }

  const updateTask = (updatedTask) => {
    const index = tasks.value.findIndex(t => t.id === updatedTask.id)
    if (index !== -1) {
      tasks.value[index] = updatedTask
    }
  }

  const removeTask = (taskId) => {
    const index = tasks.value.findIndex(t => t.id === taskId)
    if (index !== -1) {
      tasks.value.splice(index, 1)
    }
  }

  const clearError = () => {
    error.value = null
  }

  return {
    // 状态
    tasks,
    currentTask,
    loading,
    error,
    
    // 计算属性
    taskCount,
    completedTasks,
    runningTasks,
    failedTasks,
    
    // 动作
    loadTasks,
    fetchTasks, // 添加fetchTasks方法
    createTask,
    executeTask,
    cancelTask,
    getTaskDetail,
    getTaskResults,
    getTaskLogs,
    addTask,
    updateTask,
    removeTask,
    clearError
    // deleteTask 暂时移除，后端未实现
  }
})