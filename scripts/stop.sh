#!/bin/bash

# DeepResearchAgent 停止脚本

echo "🛑 停止 DeepResearchAgent 服务"
echo "================================"

# 停止后端服务
stop_backend() {
    if [ -f .backend.pid ]; then
        BACKEND_PID=$(cat .backend.pid)
        if kill -0 $BACKEND_PID 2>/dev/null; then
            echo "🛑 停止后端服务 (PID: $BACKEND_PID)..."
            kill $BACKEND_PID
            sleep 2
            
            # 强制杀死如果还在运行
            if kill -0 $BACKEND_PID 2>/dev/null; then
                echo "⚠️ 强制停止后端服务..."
                kill -9 $BACKEND_PID
            fi
            
            echo "✅ 后端服务已停止"
        else
            echo "ℹ️ 后端服务未运行"
        fi
        rm -f .backend.pid
    else
        echo "ℹ️ 未找到后端进程ID文件"
    fi
}

# 停止前端服务
stop_frontend() {
    if [ -f .frontend.pid ]; then
        FRONTEND_PID=$(cat .frontend.pid)
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            echo "🛑 停止前端服务 (PID: $FRONTEND_PID)..."
            kill $FRONTEND_PID
            sleep 2
            
            # 强制杀死如果还在运行
            if kill -0 $FRONTEND_PID 2>/dev/null; then
                echo "⚠️ 强制停止前端服务..."
                kill -9 $FRONTEND_PID
            fi
            
            echo "✅ 前端服务已停止"
        else
            echo "ℹ️ 前端服务未运行"
        fi
        rm -f .frontend.pid
    else
        echo "ℹ️ 未找到前端进程ID文件"
    fi
}

# 清理端口
cleanup_ports() {
    echo "🧹 清理端口..."
    
    # 清理后端端口 8000
    if lsof -ti:8000 > /dev/null 2>&1; then
        echo "🛑 清理端口 8000..."
        lsof -ti:8000 | xargs kill -9
    fi
    
    # 清理前端端口 3000
    if lsof -ti:3000 > /dev/null 2>&1; then
        echo "🛑 清理端口 3000..."
        lsof -ti:3000 | xargs kill -9
    fi
    
    echo "✅ 端口清理完成"
}

# 主函数
main() {
    stop_backend
    stop_frontend
    cleanup_ports
    
    echo ""
    echo "🎉 DeepResearchAgent 服务已完全停止"
    echo ""
    echo "💡 使用 ./scripts/start.sh 重新启动服务"
}

# 运行主函数
main "$@"
