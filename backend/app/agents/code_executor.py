"""
代码执行Agent
"""

import os
import sys
import subprocess
import tempfile
import asyncio
from typing import Dict, Any
import json
from datetime import datetime

from app.agents.base_agent import BaseAgent
from app.services.jupyter_service import JupyterService


class CodeExecutor(BaseAgent):
    """代码执行Agent - 执行生成的Python代码"""
    
    def __init__(self):
        super().__init__()
        self.jupyter_service = JupyterService()
    
    async def execute(self, code: str) -> Dict[str, Any]:
        """执行Python代码"""
        return await self.jupyter_service.execute_code(code)
