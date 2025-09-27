"""
WebSocket连接管理器
"""

import json
from typing import Dict, List
from fastapi import WebSocket


class WebSocketManager:
    """WebSocket连接管理器"""
    
    def __init__(self):
        self.active_connections: Dict[str, List[WebSocket]] = {}
    
    async def connect(self, websocket: WebSocket, task_id: str):
        """建立WebSocket连接"""
        await websocket.accept()
        if task_id not in self.active_connections:
            self.active_connections[task_id] = []
        self.active_connections[task_id].append(websocket)
    
    def disconnect(self, task_id: str, websocket: WebSocket = None):
        """断开WebSocket连接"""
        if task_id in self.active_connections:
            if websocket:
                self.active_connections[task_id].remove(websocket)
            else:
                # 移除所有连接
                self.active_connections[task_id].clear()
            
            # 如果没有连接了，删除任务
            if not self.active_connections[task_id]:
                del self.active_connections[task_id]
    
    async def send_message(self, task_id: str, message: dict):
        """发送消息到指定任务的所有连接"""
        if task_id in self.active_connections:
            message_text = json.dumps(message, ensure_ascii=False)
            disconnected = []
            
            for websocket in self.active_connections[task_id]:
                try:
                    await websocket.send_text(message_text)
                except:
                    disconnected.append(websocket)
            
            # 清理断开的连接
            for websocket in disconnected:
                self.active_connections[task_id].remove(websocket)
    
    async def broadcast_progress(self, task_id: str, progress: int, message: str):
        """广播进度更新"""
        await self.send_message(task_id, {
            "type": "progress",
            "progress": progress,
            "message": message
        })
    
    async def broadcast_status(self, task_id: str, status: str, data: dict = None):
        """广播状态更新"""
        message = {
            "type": "status",
            "status": status
        }
        if data:
            message["data"] = data
        
        await self.send_message(task_id, message)
    
    async def broadcast_error(self, task_id: str, error: str):
        """广播错误信息"""
        await self.send_message(task_id, {
            "type": "error",
            "error": error
        })
