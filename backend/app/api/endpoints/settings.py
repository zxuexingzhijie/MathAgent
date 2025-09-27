"""
设置相关API端点
"""

from typing import Dict, Any
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel

from app.core.database import get_db
from app.core.config import settings

router = APIRouter()


class SettingsUpdate(BaseModel):
    """设置更新模型"""
    openai_api_key: str = None
    anthropic_api_key: str = None
    default_model: str = None
    code_model: str = None
    writing_model: str = None
    analysis_model: str = None
    task_timeout: int = None
    max_concurrent_tasks: int = None


@router.get("/")
async def get_settings():
    """获取当前设置"""
    return {
        "openai_api_key": "***" if settings.OPENAI_API_KEY else None,
        "anthropic_api_key": "***" if settings.ANTHROPIC_API_KEY else None,
        "default_model": settings.DEFAULT_MODEL,
        "code_model": settings.CODE_MODEL,
        "writing_model": settings.WRITING_MODEL,
        "analysis_model": settings.ANALYSIS_MODEL,
        "task_timeout": settings.TASK_TIMEOUT,
        "max_concurrent_tasks": settings.MAX_CONCURRENT_TASKS,
        "redis_url": settings.REDIS_URL,
        "database_url": settings.DATABASE_URL,
        "upload_dir": settings.UPLOAD_DIR,
        "output_dir": settings.OUTPUT_DIR
    }


@router.put("/")
async def update_settings(settings_data: SettingsUpdate):
    """更新设置"""
    try:
        # 更新API密钥
        if settings_data.openai_api_key:
            settings.OPENAI_API_KEY = settings_data.openai_api_key
        
        if settings_data.anthropic_api_key:
            settings.ANTHROPIC_API_KEY = settings_data.anthropic_api_key
        
        # 更新模型配置
        if settings_data.default_model:
            settings.DEFAULT_MODEL = settings_data.default_model
        
        if settings_data.code_model:
            settings.CODE_MODEL = settings_data.code_model
        
        if settings_data.writing_model:
            settings.WRITING_MODEL = settings_data.writing_model
        
        if settings_data.analysis_model:
            settings.ANALYSIS_MODEL = settings_data.analysis_model
        
        # 更新其他配置
        if settings_data.task_timeout:
            settings.TASK_TIMEOUT = settings_data.task_timeout
        
        if settings_data.max_concurrent_tasks:
            settings.MAX_CONCURRENT_TASKS = settings_data.max_concurrent_tasks
        
        return {"message": "设置更新成功"}
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"更新设置失败: {str(e)}")


@router.post("/test-connection")
async def test_connection():
    """测试API连接"""
    results = {}
    
    # 测试OpenAI连接
    if settings.OPENAI_API_KEY:
        try:
            import openai
            client = openai.AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
            response = await client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[{"role": "user", "content": "Hello"}],
                max_tokens=10
            )
            results["openai"] = {"status": "success", "message": "连接成功"}
        except Exception as e:
            results["openai"] = {"status": "error", "message": str(e)}
    else:
        results["openai"] = {"status": "warning", "message": "未配置API密钥"}
    
    # 测试Anthropic连接
    if settings.ANTHROPIC_API_KEY:
        try:
            from anthropic import Anthropic
            client = Anthropic(api_key=settings.ANTHROPIC_API_KEY)
            response = client.messages.create(
                model="claude-3-haiku-20240307",
                max_tokens=10,
                messages=[{"role": "user", "content": "Hello"}]
            )
            results["anthropic"] = {"status": "success", "message": "连接成功"}
        except Exception as e:
            results["anthropic"] = {"status": "error", "message": str(e)}
    else:
        results["anthropic"] = {"status": "warning", "message": "未配置API密钥"}
    
    return results


@router.get("/models")
async def get_available_models():
    """获取可用的模型列表"""
    return {
        "openai": [
            {"id": "gpt-4", "name": "GPT-4", "description": "最强大的GPT模型"},
            {"id": "gpt-4-turbo", "name": "GPT-4 Turbo", "description": "更快的GPT-4"},
            {"id": "gpt-3.5-turbo", "name": "GPT-3.5 Turbo", "description": "快速且经济的模型"},
            {"id": "gpt-3.5-turbo-16k", "name": "GPT-3.5 Turbo 16K", "description": "支持更长上下文的模型"}
        ],
        "anthropic": [
            {"id": "claude-3-opus-20240229", "name": "Claude 3 Opus", "description": "最强大的Claude模型"},
            {"id": "claude-3-sonnet-20240229", "name": "Claude 3 Sonnet", "description": "平衡性能和速度"},
            {"id": "claude-3-haiku-20240307", "name": "Claude 3 Haiku", "description": "快速且经济的模型"}
        ]
    }
