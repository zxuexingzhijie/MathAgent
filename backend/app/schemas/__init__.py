"""
数据模式定义
"""

from .task import TaskCreate, TaskUpdate, TaskResponse
from .user import UserCreate, UserUpdate, UserResponse
from .result import ResultResponse

__all__ = [
    "TaskCreate", "TaskUpdate", "TaskResponse",
    "UserCreate", "UserUpdate", "UserResponse",
    "ResultResponse"
]
