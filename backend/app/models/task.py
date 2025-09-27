"""
任务模型
"""

from datetime import datetime
from enum import Enum
from typing import Optional, Dict, Any
from sqlalchemy import Column, Integer, String, Text, DateTime, JSON, Enum as SQLEnum
from sqlalchemy.sql import func

from app.core.database import Base


class TaskStatus(str, Enum):
    """任务状态枚举"""
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


class TaskType(str, Enum):
    """任务类型枚举"""
    MATH_MODELING = "math_modeling"
    DATA_ANALYSIS = "data_analysis"
    OPTIMIZATION = "optimization"
    SIMULATION = "simulation"


class Task(Base):
    """任务模型"""
    __tablename__ = "tasks"
    
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255), nullable=False, comment="任务标题")
    description = Column(Text, nullable=False, comment="任务描述")
    task_type = Column(SQLEnum(TaskType), nullable=False, comment="任务类型")
    status = Column(SQLEnum(TaskStatus), default=TaskStatus.PENDING, comment="任务状态")
    
    # 输入数据
    input_data = Column(JSON, comment="输入数据")
    problem_statement = Column(Text, comment="问题陈述")
    constraints = Column(Text, comment="约束条件")
    
    # 处理过程
    analysis_result = Column(JSON, comment="分析结果")
    code_generated = Column(Text, comment="生成的代码")
    execution_result = Column(JSON, comment="执行结果")
    
    # 输出结果
    paper_content = Column(Text, comment="论文内容")
    output_files = Column(JSON, comment="输出文件路径")
    
    # 元数据
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")
    started_at = Column(DateTime, comment="开始时间")
    completed_at = Column(DateTime, comment="完成时间")
    
    # 用户关联
    user_id = Column(Integer, nullable=True, comment="用户ID")
    
    # 配置
    config = Column(JSON, comment="任务配置")
    error_message = Column(Text, comment="错误信息")
    
    # 关联关系
    results = relationship("Result", back_populates="task")
    
    def __repr__(self):
        return f"<Task(id={self.id}, title='{self.title}', status='{self.status}')>"
