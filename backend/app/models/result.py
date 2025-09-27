"""
结果模型
"""

from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime, JSON, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.core.database import Base


class Result(Base):
    """结果模型"""
    __tablename__ = "results"
    
    id = Column(Integer, primary_key=True, index=True)
    task_id = Column(Integer, ForeignKey("tasks.id"), nullable=False, comment="任务ID")
    
    # 结果类型
    result_type = Column(String(50), nullable=False, comment="结果类型")
    title = Column(String(255), comment="结果标题")
    content = Column(Text, comment="结果内容")
    
    # 数据
    data = Column(JSON, comment="结果数据")
    metadata = Column(JSON, comment="元数据")
    
    # 文件
    file_path = Column(String(500), comment="文件路径")
    file_type = Column(String(50), comment="文件类型")
    
    # 时间戳
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    
    # 关联关系
    task = relationship("Task", back_populates="results")
    
    def __repr__(self):
        return f"<Result(id={self.id}, task_id={self.task_id}, type='{self.result_type}')>"
