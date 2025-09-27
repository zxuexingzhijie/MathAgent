"""
任务相关的数据模式
"""

from datetime import datetime
from typing import Optional, Dict, Any
from pydantic import BaseModel

from app.models.task import TaskStatus, TaskType


class TaskBase(BaseModel):
    """任务基础模式"""
    title: str
    description: str
    task_type: TaskType
    problem_statement: Optional[str] = None
    constraints: Optional[str] = None
    input_data: Optional[Dict[str, Any]] = None
    config: Optional[Dict[str, Any]] = None


class TaskCreate(TaskBase):
    """创建任务模式"""
    pass


class TaskUpdate(BaseModel):
    """更新任务模式"""
    title: Optional[str] = None
    description: Optional[str] = None
    status: Optional[TaskStatus] = None
    analysis_result: Optional[Dict[str, Any]] = None
    code_generated: Optional[str] = None
    execution_result: Optional[Dict[str, Any]] = None
    paper_content: Optional[str] = None
    output_files: Optional[Dict[str, Any]] = None
    error_message: Optional[str] = None


class TaskResponse(TaskBase):
    """任务响应模式"""
    id: int
    status: TaskStatus
    analysis_result: Optional[Dict[str, Any]] = None
    code_generated: Optional[str] = None
    execution_result: Optional[Dict[str, Any]] = None
    paper_content: Optional[str] = None
    output_files: Optional[Dict[str, Any]] = None
    created_at: datetime
    updated_at: datetime
    started_at: Optional[datetime] = None
    completed_at: Optional[datetime] = None
    user_id: Optional[int] = None
    error_message: Optional[str] = None
    
    class Config:
        from_attributes = True
