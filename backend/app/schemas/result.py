"""
结果相关的数据模式
"""

from datetime import datetime
from typing import Optional, Dict, Any
from pydantic import BaseModel


class ResultResponse(BaseModel):
    """结果响应模式"""
    id: int
    task_id: int
    result_type: str
    title: Optional[str] = None
    content: Optional[str] = None
    data: Optional[Dict[str, Any]] = None
    metadata: Optional[Dict[str, Any]] = None
    file_path: Optional[str] = None
    file_type: Optional[str] = None
    created_at: datetime
    
    class Config:
        from_attributes = True
