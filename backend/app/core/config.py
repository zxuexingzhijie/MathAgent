"""
配置管理模块
"""

import os
from typing import Optional
from pydantic import BaseSettings


class Settings(BaseSettings):
    """应用配置"""
    
    # API配置
    OPENAI_API_KEY: Optional[str] = None
    ANTHROPIC_API_KEY: Optional[str] = None
    
    # Redis配置
    REDIS_URL: str = "redis://localhost:6379"
    
    # 服务配置
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = True
    
    # 数据库配置
    DATABASE_URL: str = "mysql+pymysql://root:password@localhost:3306/deepresearch"
    
    # 文件存储
    UPLOAD_DIR: str = "./uploads"
    OUTPUT_DIR: str = "./outputs"
    
    # Agent配置
    MAX_CONCURRENT_TASKS: int = 5
    TASK_TIMEOUT: int = 3600
    
    # 模型配置
    DEFAULT_MODEL: str = "gpt-4"
    CODE_MODEL: str = "gpt-4"
    ANALYSIS_MODEL: str = "gpt-4"
    WRITING_MODEL: str = "gpt-4"
    
    class Config:
        env_file = ".env"
        case_sensitive = True


# 全局配置实例
settings = Settings()
