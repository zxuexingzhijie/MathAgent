import { ref } from 'vue'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

class WebSocketService {
  constructor() {
    this.stompClient = null
    this.subscription = null
    this.connected = ref(false)
    this.messages = ref([])
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000
  }

  /**
   * 连接 WebSocket (使用 STOMP over SockJS)
   * @param {string} taskId - 任务ID
   */
  connect(taskId) {
    const socketUrl = 'http://localhost:8080/api/ws'
    
    try {
      // 创建 SockJS 连接
      const socket = new SockJS(socketUrl)
      this.stompClient = Stomp.over(socket)
      
      // 禁用调试输出(可选)
      // this.stompClient.debug = () => {}
      
      // 连接到 STOMP 服务器
      this.stompClient.connect(
        {},
        (frame) => {
          console.log('STOMP 连接成功:', frame)
          this.connected.value = true
          this.reconnectAttempts = 0
          
          // 订阅任务专属主题
          const destination = `/topic/task/${taskId}`
          this.subscription = this.stompClient.subscribe(destination, (message) => {
            try {
              const parsedMessage = JSON.parse(message.body)
              console.log('收到 WebSocket 消息:', parsedMessage)
              this.messages.value.push(parsedMessage)
              
              // 触发自定义事件
              this.onMessage(parsedMessage)
            } catch (error) {
              console.error('解析 WebSocket 消息失败:', error)
            }
          })
          
          console.log(`已订阅主题: ${destination}`)
        },
        (error) => {
          console.error('STOMP 连接错误:', error)
          this.connected.value = false
          this.onError(error)
          this.attemptReconnect(taskId)
        }
      )
    } catch (error) {
      console.error('创建 WebSocket 连接失败:', error)
      this.attemptReconnect(taskId)
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
  send(destination, message) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(destination, {}, JSON.stringify(message))
    } else {
      console.warn('STOMP 客户端未连接')
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.subscription) {
      this.subscription.unsubscribe()
      this.subscription = null
    }
    
    if (this.stompClient) {
      this.stompClient.disconnect(() => {
        console.log('STOMP 连接已断开')
      })
      this.stompClient = null
    }
    
    this.connected.value = false
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

// 导出单例
export default new WebSocketService()
