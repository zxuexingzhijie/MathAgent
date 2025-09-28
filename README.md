# 数学建模DeepResearch Agent

基于Spring AI Alibaba Graph框架和Vue.js的智能数学建模研究系统，集成Multi-Agents和Code Interpreter。

![image](./image/系统架构图.png)
## 系统架构

### 后端技术栈
- **Spring Boot 3.2.0** - 主框架
- **Spring AI Alibaba 1.0.0.4** - AI Agent框架
- **MyBatis 3.0.3** - 数据访问层
- **MySQL 8.0** - 生产数据库
- **DashScope** - 阿里云大模型服务
- **Multi-LLMs** - 多模型配置
- **Code Interpreter** - 基于Jupyter的代码执行

### 前端技术栈
- **Vue 3.4.0** - 前端框架
- **Vue Router 4.2.0** - 路由管理
- **Pinia 2.1.0** - 状态管理
- **Element Plus 2.4.0** - UI组件库
- **ECharts 5.4.0** - 图表库
- **Axios 1.6.0** - HTTP客户端

## 核心功能

### 🔍 自动分析问题
- **建模手Agent**: 专业数学建模和问题分析
- **智能识别**: 问题类型、复杂度、变量定义
- **模型构建**: 目标函数、约束条件、求解策略

### 💻 Code Interpreter
- **Jupyter集成**: 基于Jupyter的代码执行环境
- **代码手Agent**: 自动生成Python求解代码
- **实时执行**: 代码执行、错误调试、结果验证
- **Notebook保存**: 代码保存为可编辑的notebook

### 📝 生成编排好格式的论文
- **论文手Agent**: 专业学术论文撰写
- **格式化输出**: LaTeX格式、学术规范
- **完整结构**: 摘要、引言、模型、求解、结论
- **多格式支持**: PDF、Word、Markdown

### 🤝 Multi-Agents系统
- **建模手**: 数学建模专家，使用qwen-max模型
- **代码手**: 代码工程师，使用qwen-plus模型  
- **论文手**: 学术写作专家，使用qwen-max模型
- **协同工作**: Graph工作流编排，并行执行

### 🔄 Multi-LLMs配置
- **专业分工**: 每个Agent使用最适合的模型
- **参数优化**: 不同任务使用不同的温度和token数
- **性能平衡**: 精确性、创造性、效率的平衡

## Graph工作流

系统采用Spring AI Alibaba的Graph框架，实现以下Multi-Agents工作流：

```
问题输入 → 建模手Agent → 代码手Agent → 论文手Agent → 完整输出
```

每个Agent都是独立的专业模块，可以并行执行和错误恢复。

## 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- Maven 3.6+
- MySQL 8.0+
