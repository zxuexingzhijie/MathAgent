"""
基础Agent类
"""

import asyncio
from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
import openai
from anthropic import Anthropic

from app.core.config import settings


class BaseAgent(ABC):
    """基础Agent抽象类"""
    
    def __init__(self):
        self.openai_client = openai.AsyncOpenAI(
            api_key=settings.OPENAI_API_KEY
        ) if settings.OPENAI_API_KEY else None
        
        self.anthropic_client = Anthropic(
            api_key=settings.ANTHROPIC_API_KEY
        ) if settings.ANTHROPIC_API_KEY else None
    
    async def call_llm(
        self, 
        prompt: str, 
        model: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 4000
    ) -> str:
        """调用LLM API"""
        if not model:
            model = settings.DEFAULT_MODEL
        
        try:
            if model.startswith("claude"):
                # 使用Anthropic API
                if not self.anthropic_client:
                    raise ValueError("Anthropic API key not configured")
                
                response = await self.anthropic_client.messages.create(
                    model=model,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    messages=[{"role": "user", "content": prompt}]
                )
                return response.content[0].text
            
            else:
                # 使用OpenAI API
                if not self.openai_client:
                    raise ValueError("OpenAI API key not configured")
                
                response = await self.openai_client.chat.completions.create(
                    model=model,
                    messages=[{"role": "user", "content": prompt}],
                    temperature=temperature,
                    max_tokens=max_tokens
                )
                return response.choices[0].message.content
        
        except Exception as e:
            raise Exception(f"LLM API调用失败: {str(e)}")
    
    @abstractmethod
    async def process(self, *args, **kwargs) -> Any:
        """处理逻辑的抽象方法"""
        pass
