"""
任务相关API端点
"""

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.models.task import Task, TaskStatus, TaskType
from app.schemas.task import TaskCreate, TaskResponse, TaskUpdate
from app.services.task_service import TaskService
from app.services.websocket_manager import WebSocketManager

router = APIRouter()


@router.post("/", response_model=TaskResponse)
async def create_task(
    task_data: TaskCreate,
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """创建新任务"""
    task_service = TaskService(db)
    task = await task_service.create_task(task_data)
    
    # 在后台启动任务处理
    background_tasks.add_task(task_service.process_task, task.id)
    
    return task


@router.get("/", response_model=List[TaskResponse])
async def get_tasks(
    skip: int = 0,
    limit: int = 100,
    status: Optional[TaskStatus] = None,
    db: Session = Depends(get_db)
):
    """获取任务列表"""
    task_service = TaskService(db)
    tasks = await task_service.get_tasks(skip=skip, limit=limit, status=status)
    return tasks


@router.get("/{task_id}", response_model=TaskResponse)
async def get_task(task_id: int, db: Session = Depends(get_db)):
    """获取单个任务详情"""
    task_service = TaskService(db)
    task = await task_service.get_task(task_id)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task


@router.put("/{task_id}", response_model=TaskResponse)
async def update_task(
    task_id: int,
    task_data: TaskUpdate,
    db: Session = Depends(get_db)
):
    """更新任务"""
    task_service = TaskService(db)
    task = await task_service.update_task(task_id, task_data)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task


@router.delete("/{task_id}")
async def delete_task(task_id: int, db: Session = Depends(get_db)):
    """删除任务"""
    task_service = TaskService(db)
    success = await task_service.delete_task(task_id)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found")
    return {"message": "Task deleted successfully"}


@router.post("/{task_id}/cancel")
async def cancel_task(task_id: int, db: Session = Depends(get_db)):
    """取消任务"""
    task_service = TaskService(db)
    success = await task_service.cancel_task(task_id)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found")
    return {"message": "Task cancelled successfully"}


@router.get("/{task_id}/status")
async def get_task_status(task_id: int, db: Session = Depends(get_db)):
    """获取任务状态"""
    task_service = TaskService(db)
    task = await task_service.get_task(task_id)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return {
        "task_id": task_id,
        "status": task.status,
        "progress": getattr(task, 'progress', 0),
        "message": getattr(task, 'current_message', '')
    }
