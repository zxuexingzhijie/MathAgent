# DeepResearchAgent - 数学建模深度研究助手

🤖📐 专为数学建模比赛设计的深度研究Agent，自动完成从问题分析到论文生成的完整流程。

## 🌟 核心特性

- 🔍 **智能问题分析**：自动解析数学建模题目，识别问题类型和约束条件
- 💻 **代码自动生成**：基于问题分析自动生成Python代码，支持多种算法
- 📊 **数据可视化**：自动生成图表和可视化结果
- 📝 **论文自动撰写**：生成结构完整的数学建模论文
- 🔄 **实时进度跟踪**：WebSocket实时显示研究进度
- 🧩 **模块化设计**：各Agent独立工作，易于扩展

## 🚀 快速开始

### 环境要求

- Python 3.9+
- Node.js 16+
- Redis (用于任务队列)

### 安装步骤

1. **克隆项目**
```bash
git clone <your-repo-url>
cd DeepResearchAgent
```

2. **安装后端依赖**
```bash
cd backend
pip install -r requirements.txt
```

3. **安装前端依赖**
```bash
cd frontend
npm install
```

4. **启动Redis服务**
```bash
# Windows
redis-server

# macOS (使用Homebrew)
brew services start redis

# Linux
sudo systemctl start redis
```

5. **启动后端服务**
```bash
cd backend
python main.py
```

6. **启动前端服务**
```bash
cd frontend
npm run dev
```

7. **访问应用**
- 前端界面：http://localhost:3000
- 后端API：http://localhost:8000

## 📁 项目结构

```
DeepResearchAgent/
├── backend/                 # 后端服务
│   ├── app/
│   │   ├── agents/         # Agent系统
│   │   ├── core/          # 核心功能
│   │   ├── models/        # 数据模型
│   │   └── services/      # 业务服务
│   ├── requirements.txt
│   └── main.py
├── frontend/               # 前端应用
│   ├── src/
│   │   ├── components/    # Vue组件
│   │   ├── views/         # 页面视图
│   │   └── services/      # API服务
│   ├── package.json
│   └── vite.config.js
├── docs/                  # 文档
└── README.md
```

## 🔧 配置说明

### 后端配置

在 `backend/.env` 文件中配置：

```env
# API配置
OPENAI_API_KEY=your_openai_api_key
ANTHROPIC_API_KEY=your_anthropic_api_key

# Redis配置
REDIS_URL=redis://localhost:6379

# 服务配置
HOST=0.0.0.0
PORT=8000
```

### 前端配置

在 `frontend/.env` 文件中配置：

```env
VITE_API_BASE_URL=http://localhost:8000
VITE_WS_URL=ws://localhost:8000/ws
```

## 🎯 使用流程

1. **输入问题**：在Web界面输入数学建模题目
2. **自动分析**：Agent自动分析问题类型和需求
3. **生成代码**：自动生成Python代码并执行
4. **生成论文**：基于分析结果自动撰写论文
5. **下载结果**：下载完整的论文和代码

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 🙏 致谢

感谢开源社区的支持和贡献。