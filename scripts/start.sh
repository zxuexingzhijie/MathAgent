#!/bin/bash

# DeepResearchAgent 启动脚本

set -e

echo "🚀 启动 DeepResearchAgent 服务"
echo "================================"

# 检查配置文件
check_config() {
    if [ ! -f backend/.env ]; then
        echo "❌ 后端配置文件不存在，请先运行安装脚本"
        exit 1
    fi
    
    if [ ! -f frontend/.env ]; then
        echo "❌ 前端配置文件不存在，请先运行安装脚本"
        exit 1
    fi
    
    echo "✅ 配置文件检查通过"
}

# 检查服务状态
check_services() {
    echo "🔍 检查服务状态..."
    
    # 检查MySQL
    if ! pgrep -x "mysqld" > /dev/null; then
        echo "⚠️ MySQL未运行，正在启动..."
        case $OS in
            "ubuntu"|"centos")
                sudo systemctl start mysql
                ;;
            "macos")
                brew services start mysql
                ;;
        esac
    fi
    
    # 检查Redis
    if ! pgrep -x "redis-server" > /dev/null; then
        echo "⚠️ Redis未运行，正在启动..."
        case $OS in
            "ubuntu"|"centos")
                sudo systemctl start redis
                ;;
            "macos")
                brew services start redis
                ;;
        esac
    fi
    
    echo "✅ 服务状态检查完成"
}

# 启动后端服务
start_backend() {
    echo "🐍 启动后端服务..."
    
    cd backend
    
    # 激活虚拟环境
    if [ ! -d "venv" ]; then
        echo "❌ 虚拟环境不存在，请先运行安装脚本"
        exit 1
    fi
    
    source venv/bin/activate
    
    # 检查依赖
    pip check
    
    # 启动服务
    python main.py &
    BACKEND_PID=$!
    
    cd ..
    
    # 等待服务启动
    echo "⏳ 等待后端服务启动..."
    sleep 5
    
    # 检查服务是否启动成功
    if curl -s http://localhost:8000/health > /dev/null; then
        echo "✅ 后端服务启动成功 (PID: $BACKEND_PID)"
    else
        echo "❌ 后端服务启动失败"
        exit 1
    fi
}

# 启动前端服务
start_frontend() {
    echo "📦 启动前端服务..."
    
    cd frontend
    
    # 检查依赖
    if [ ! -d "node_modules" ]; then
        echo "❌ 前端依赖不存在，请先运行安装脚本"
        exit 1
    fi
    
    # 启动服务
    npm run dev &
    FRONTEND_PID=$!
    
    cd ..
    
    # 等待服务启动
    echo "⏳ 等待前端服务启动..."
    sleep 10
    
    # 检查服务是否启动成功
    if curl -s http://localhost:3000 > /dev/null; then
        echo "✅ 前端服务启动成功 (PID: $FRONTEND_PID)"
    else
        echo "❌ 前端服务启动失败"
        exit 1
    fi
}

# 保存进程ID
save_pids() {
    echo $BACKEND_PID > .backend.pid
    echo $FRONTEND_PID > .frontend.pid
    echo "💾 进程ID已保存"
}

# 显示服务信息
show_info() {
    echo ""
    echo "🎉 DeepResearchAgent 启动完成！"
    echo ""
    echo "🌐 服务地址："
    echo "   前端界面: http://localhost:3000"
    echo "   后端API:  http://localhost:8000"
    echo "   API文档:  http://localhost:8000/docs"
    echo ""
    echo "📊 服务状态："
    echo "   后端进程: $BACKEND_PID"
    echo "   前端进程: $FRONTEND_PID"
    echo ""
    echo "💡 使用 ./scripts/stop.sh 停止服务"
    echo "📖 查看日志: tail -f backend/logs/app.log"
}

# 主函数
main() {
    # 检测操作系统
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
        OS="unknown"
    fi
    
    check_config
    check_services
    start_backend
    start_frontend
    save_pids
    show_info
}

# 运行主函数
main "$@"
