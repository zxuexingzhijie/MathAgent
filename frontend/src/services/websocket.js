import { ref } from 'vue'

class WebSocketService {
  constructor() {
    this.ws = null
    this.connected = ref(false)
    this.messages = ref([])
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
  }

  /**
   * 连接 WebSocket
   * @param {string} taskId - 任务ID
   */
  connect(taskId) {
    const wsUrl = `ws://localhost:8080/api/ws/modeling/${taskId}`
    
    try {
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket 连接成功')
        this.connected.value = true
        this.reconnectAttempts = 0
      }
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('收到 WebSocket 消息:', message)
          this.messages.value.push(message)
          
          // 触发自定义事件
          this.onMessage(message)
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.onError(error)
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.connected.value = false
        this.attemptReconnect(taskId)
      }
      
    } catch (error) {
      console.error('创建 WebSocket 连接失败:', error)
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect(taskId) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect(taskId)
      }, this.reconnectDelay)
    } else {
      console.error('达到最大重连次数，停止重连')
    }
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket 未连接')
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 消息处理回调（由外部设置）
   */
  onMessage(message) {
    // 由外部组件覆盖此方法
  }

  /**
   * 错误处理回调（由外部设置）
   */
  onError(error) {
    // 由外部组件覆盖此方法
  }
}

export default new WebSocketService()
