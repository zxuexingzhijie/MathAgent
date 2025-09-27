import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { taskApi } from '../services/api'

export const useTaskStore = defineStore('task', () => {
  // 状态
  const tasks = ref([])
  const currentTask = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const taskCount = computed(() => tasks.value.length)
  const completedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'completed')
  )
  const runningTasks = computed(() => 
    tasks.value.filter(task => task.status === 'running')
  )

  // 操作
  const fetchTasks = async (params = {}) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await taskApi.getTasks(params)
      tasks.value = response.data
    } catch (err) {
      error.value = err.message
      console.error('获取任务列表失败:', err)
    } finally {
      loading.value = false
    }
  }

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

  const fetchTask = async (taskId) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await taskApi.getTask(taskId)
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

  const updateTask = async (taskId, updateData) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await taskApi.updateTask(taskId, updateData)
      const updatedTask = response.data
      
      // 更新任务列表中的任务
      const index = tasks.value.findIndex(task => task.id === taskId)
      if (index !== -1) {
        tasks.value[index] = updatedTask
      }
      
      // 更新当前任务
      if (currentTask.value && currentTask.value.id === taskId) {
        currentTask.value = updatedTask
      }
      
      return updatedTask
    } catch (err) {
      error.value = err.message
      console.error('更新任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const cancelTask = async (taskId) => {
    loading.value = true
    error.value = null
    
    try {
      await taskApi.cancelTask(taskId)
      
      // 更新任务状态
      const task = tasks.value.find(t => t.id === taskId)
      if (task) {
        task.status = 'cancelled'
      }
      
      if (currentTask.value && currentTask.value.id === taskId) {
        currentTask.value.status = 'cancelled'
      }
    } catch (err) {
      error.value = err.message
      console.error('取消任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const deleteTask = async (taskId) => {
    loading.value = true
    error.value = null
    
    try {
      await taskApi.deleteTask(taskId)
      
      // 从任务列表中移除
      const index = tasks.value.findIndex(task => task.id === taskId)
      if (index !== -1) {
        tasks.value.splice(index, 1)
      }
      
      // 清除当前任务
      if (currentTask.value && currentTask.value.id === taskId) {
        currentTask.value = null
      }
    } catch (err) {
      error.value = err.message
      console.error('删除任务失败:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  const clearCurrentTask = () => {
    currentTask.value = null
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
    
    // 操作
    fetchTasks,
    createTask,
    fetchTask,
    updateTask,
    cancelTask,
    deleteTask,
    clearError,
    clearCurrentTask
  }
})
