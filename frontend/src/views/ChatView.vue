<template>
  <div class="chat-view">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
        <h3>开始对话</h3>
        <p>我是数学建模专家，可以帮您分析问题、建立模型、编写代码并生成论文</p>
        
        <div class="quick-prompts">
          <el-tag
            v-for="prompt in quickPrompts"
            :key="prompt"
            @click="sendQuickPrompt(prompt)"
            class="prompt-tag"
          >
            {{ prompt }}
          </el-tag>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-for="message in messages" :key="message.id" class="message-wrapper">
        <!-- 用户消息 -->
        <div v-if="message.role === 'USER'" class="message user-message">
          <div class="message-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div v-if="message.attachmentData" class="message-attachment">
              <el-icon><Paperclip /></el-icon>
              附件已上传
            </div>
          </div>
        </div>

        <!-- 助手消息 -->
        <div v-else class="message assistant-message">
          <div class="message-avatar assistant-avatar">
            <el-icon><Robot /></el-icon>
          </div>
          <div class="message-content">
            <!-- 建模分析 -->
            <div v-if="message.type === 'MODELING'" class="modeling-result">
              <div class="result-header">
                <el-icon><DataAnalysis /></el-icon>
                <span>建模分析</span>
              </div>
              <div class="result-content" v-html="renderMarkdown(message.content)"></div>
            </div>

            <!-- 代码执行 -->
            <div v-else-if="message.type === 'EXECUTION'" class="execution-result">
              <div class="result-header">
                <el-icon><Monitor /></el-icon>
                <span>代码执行</span>
              </div>
              <div class="result-content" v-html="renderMarkdown(message.content)"></div>
            </div>

            <!-- 论文生成 -->
            <div v-else-if="message.type === 'PAPER'" class="paper-result">
              <div class="result-header">
                <el-icon><Document /></el-icon>
                <span>论文生成</span>
              </div>
              <div class="result-content" v-html="renderMarkdown(message.content)"></div>
              <div class="paper-actions">
                <el-button size="small" @click="exportPaper">
                  <el-icon><Download /></el-icon>
                  导出论文
                </el-button>
              </div>
            </div>

            <!-- 普通文本 -->
            <div v-else class="message-text" v-html="renderMarkdown(message.content)"></div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isProcessing" class="message assistant-message">
        <div class="message-avatar assistant-avatar">
          <el-icon><Robot /></el-icon>
        </div>
        <div class="message-content">
          <div class="loading-message">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ currentStage }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入您的数学建模问题..."
          @keydown.enter.ctrl="sendMessage"
          :disabled="isProcessing"
        />
        
        <div class="input-actions">
          <div class="left-actions">
            <el-upload
              :before-upload="handleFileUpload"
              :show-file-list="false"
              accept=".txt,.csv,.xlsx,.pdf"
            >
              <el-button size="small" :disabled="isProcessing">
                <el-icon><Paperclip /></el-icon>
                上传文件
              </el-button>
            </el-upload>
            
            <el-tag v-if="uploadedFile" closable @close="removeFile" size="small">
              {{ uploadedFile.name }}
            </el-tag>
          </div>

          <div class="right-actions">
            <el-button
              v-if="isProcessing"
              type="warning"
              @click="stopGeneration"
            >
              <el-icon><Stop /></el-icon>
              停止生成
            </el-button>
            <el-button
              v-else
              type="primary"
              @click="sendMessage"
              :disabled="!inputMessage.trim() && !uploadedFile"
            >
              <el-icon><Send /></el-icon>
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import api from '../services/api'

const route = useRoute()
const sessionId = ref(route.params.sessionId)

const messages = ref([])
const inputMessage = ref('')
const isProcessing = ref(false)
const currentStage = ref('思考中...')
const messagesContainer = ref(null)
const uploadedFile = ref(null)
const uploadedFileData = ref(null)

const quickPrompts = [
  '优化问题求解',
  '数据分析与建模',
  '预测模型构建',
  '资源分配问题'
]

// 加载消息历史
onMounted(async () => {
  await loadMessages()
  scrollToBottom()
})

// 监听消息变化，自动滚动
watch(() => messages.value.length, () => {
  nextTick(() => scrollToBottom())
})

const loadMessages = async () => {
  try {
    const response = await api.get(`/chat/sessions/${sessionId.value}/messages`)
    messages.value = response.data
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() && !uploadedFile.value) return

  const content = inputMessage.value
  const fileData = uploadedFileData.value

  // 清空输入
  inputMessage.value = ''
  uploadedFile.value = null
  uploadedFileData.value = null

  isProcessing.value = true
  currentStage.value = '正在处理...'

  try {
    // 使用 fetch API 配合 ReadableStream 接收 SSE
    const response = await fetch(`/api/chat/sessions/${sessionId.value}/messages`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        content: content,
        fileData: fileData || null
      })
    })

    if (!response.ok) {
      throw new Error('请求失败')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        isProcessing.value = false
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          try {
            const data = JSON.parse(line.substring(5))
            handleSseMessage(data)
          } catch (e) {
            console.error('解析SSE消息失败:', e)
          }
        } else if (line.startsWith('event:')) {
          // 事件类型行，下一行是数据
          continue
        }
      }
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    isProcessing.value = false
  }
}

const handleSseMessage = (data) => {
  const { event, payload } = data

  switch (event) {
    case 'user_message':
    case 'modeling':
    case 'coding':
    case 'paper':
    case 'message':
      messages.value.push(payload)
      break
    case 'stage':
      currentStage.value = payload.message
      break
    case 'error':
      ElMessage.error(payload.message || '处理失败')
      isProcessing.value = false
      break
  }
}

const sendQuickPrompt = (prompt) => {
  inputMessage.value = prompt
  sendMessage()
}

const handleFileUpload = (file) => {
  uploadedFile.value = file

  // 读取文件内容
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedFileData.value = e.target.result
  }
  reader.readAsText(file)

  return false // 阻止自动上传
}

const removeFile = () => {
  uploadedFile.value = null
  uploadedFileData.value = null
}

const stopGeneration = async () => {
  try {
    await api.post(`/chat/sessions/${sessionId.value}/stop`)
    isProcessing.value = false
    ElMessage.success('已停止生成')
  } catch (error) {
    console.error('停止生成失败:', error)
  }
}

const exportPaper = async () => {
  try {
    const response = await api.get(`/chat/sessions/${sessionId.value}/export`)
    const blob = new Blob([response.data], { type: 'text/markdown' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `论文_${Date.now()}.md`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const renderMarkdown = (content) => {
  return marked(content || '')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>

<style lang="scss" scoped>
.chat-view {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  .empty-state {
    text-align: center;
    padding: 80px 24px;

    .empty-icon {
      font-size: 64px;
      color: #c0c4cc;
      margin-bottom: 16px;
    }

    h3 {
      font-size: 24px;
      color: #303133;
      margin: 16px 0 8px;
    }

    p {
      font-size: 14px;
      color: #909399;
      margin-bottom: 32px;
    }

    .quick-prompts {
      display: flex;
      gap: 12px;
      justify-content: center;
      flex-wrap: wrap;

      .prompt-tag {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}

.message-wrapper {
  margin-bottom: 24px;
}

.message {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s;

  .message-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #e9ecef;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.assistant-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
  }

  .message-content {
    flex: 1;
    max-width: 80%;
  }

  .message-text {
    background: white;
    padding: 16px;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    line-height: 1.6;
  }

  .message-attachment {
    margin-top: 8px;
    padding: 8px 12px;
    background: #f0f0f0;
    border-radius: 8px;
    font-size: 12px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.user-message {
  flex-direction: row-reverse;

  .message-content {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  .message-text {
    background: #4285f4;
    color: white;
  }
}

.modeling-result, .execution-result, .paper-result {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .result-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
  }

  .result-content {
    padding: 16px;
    line-height: 1.6;
  }

  .paper-actions {
    padding: 12px 16px;
    border-top: 1px solid #e9ecef;
    display: flex;
    justify-content: flex-end;
  }
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chat-input-area {
  background: white;
  border-top: 1px solid #e9ecef;
  padding: 16px 24px;

  .input-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;

    .left-actions, .right-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
