#!/bin/bash

# DeepResearchAgent 安装脚本
# 支持 Ubuntu/Debian, CentOS/RHEL, macOS

set -e

echo "🚀 DeepResearchAgent 安装脚本"
echo "================================"

# 检测操作系统
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            OS="ubuntu"
        elif command -v yum &> /dev/null; then
            OS="centos"
        else
            OS="linux"
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    else
        echo "❌ 不支持的操作系统: $OSTYPE"
        exit 1
    fi
    echo "✅ 检测到操作系统: $OS"
}

# 安装系统依赖
install_system_deps() {
    echo "📦 安装系统依赖..."
    
    case $OS in
        "ubuntu")
            sudo apt-get update
            sudo apt-get install -y python3 python3-pip python3-venv nodejs npm redis-server mysql-server git curl
            ;;
        "centos")
            sudo yum update -y
            sudo yum install -y python3 python3-pip nodejs npm redis mysql-server git curl
            ;;
        "macos")
            if ! command -v brew &> /dev/null; then
                echo "📦 安装 Homebrew..."
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            brew install python@3.9 node redis mysql git
            ;;
    esac
    
    echo "✅ 系统依赖安装完成"
}

# 配置MySQL
setup_mysql() {
    echo "🗄️ 配置MySQL数据库..."
    
    case $OS in
        "ubuntu"|"centos")
            sudo systemctl start mysql
            sudo systemctl enable mysql
            ;;
        "macos")
            brew services start mysql
            ;;
    esac
    
    # 创建数据库和用户
    echo "请设置MySQL root密码（如果还没有设置）:"
    mysql -u root -p < scripts/setup_mysql.sql
    
    echo "✅ MySQL配置完成"
}

# 配置Redis
setup_redis() {
    echo "🔴 配置Redis..."
    
    case $OS in
        "ubuntu"|"centos")
            sudo systemctl start redis
            sudo systemctl enable redis
            ;;
        "macos")
            brew services start redis
            ;;
    esac
    
    echo "✅ Redis配置完成"
}

# 安装Python依赖
install_python_deps() {
    echo "🐍 安装Python依赖..."
    
    cd backend
    
    # 创建虚拟环境
    python3 -m venv venv
    source venv/bin/activate
    
    # 升级pip
    pip install --upgrade pip
    
    # 安装依赖
    pip install -r requirements.txt
    
    cd ..
    echo "✅ Python依赖安装完成"
}

# 安装Node.js依赖
install_node_deps() {
    echo "📦 安装Node.js依赖..."
    
    cd frontend
    npm install
    cd ..
    
    echo "✅ Node.js依赖安装完成"
}

# 创建配置文件
create_config() {
    echo "⚙️ 创建配置文件..."
    
    # 后端配置
    if [ ! -f backend/.env ]; then
        cp backend/env.example backend/.env
        echo "📝 请编辑 backend/.env 文件配置API密钥和数据库连接"
    fi
    
    # 前端配置
    if [ ! -f frontend/.env ]; then
        cp frontend/env.example frontend/.env
        echo "📝 请编辑 frontend/.env 文件配置API地址"
    fi
    
    # 创建必要的目录
    mkdir -p backend/uploads
    mkdir -p backend/outputs
    mkdir -p backend/kernels
    
    echo "✅ 配置文件创建完成"
}

# 启动服务
start_services() {
    echo "🚀 启动服务..."
    
    # 启动后端服务
    cd backend
    source venv/bin/activate
    python main.py &
    BACKEND_PID=$!
    cd ..
    
    # 等待后端启动
    sleep 5
    
    # 启动前端服务
    cd frontend
    npm run dev &
    FRONTEND_PID=$!
    cd ..
    
    echo "✅ 服务启动完成"
    echo "🌐 前端地址: http://localhost:3000"
    echo "🔧 后端地址: http://localhost:8000"
    echo "📚 API文档: http://localhost:8000/docs"
    
    # 保存进程ID
    echo $BACKEND_PID > .backend.pid
    echo $FRONTEND_PID > .frontend.pid
    
    echo "💡 使用 ./scripts/stop.sh 停止服务"
}

# 主安装流程
main() {
    detect_os
    install_system_deps
    setup_mysql
    setup_redis
    install_python_deps
    install_node_deps
    create_config
    
    echo ""
    echo "🎉 DeepResearchAgent 安装完成！"
    echo ""
    echo "📋 下一步操作："
    echo "1. 编辑 backend/.env 配置API密钥"
    echo "2. 编辑 frontend/.env 配置API地址"
    echo "3. 运行 ./scripts/start.sh 启动服务"
    echo ""
    echo "📖 更多信息请查看 README.md"
}

# 运行主函数
main "$@"
