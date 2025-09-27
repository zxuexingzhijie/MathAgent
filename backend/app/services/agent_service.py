"""
Agent服务 - 核心AI处理逻辑
"""

import json
import os
from typing import Dict, Any, Optional
import asyncio
from datetime import datetime

from app.core.config import settings
from app.models.task import Task
from app.agents.problem_analyzer import ProblemAnalyzer
from app.agents.code_generator import CodeGenerator
from app.agents.code_executor import CodeExecutor
from app.agents.paper_writer import PaperWriter


class AgentService:
    """Agent服务类 - 协调各个Agent完成数学建模任务"""
    
    def __init__(self):
        self.problem_analyzer = ProblemAnalyzer()
        self.code_generator = CodeGenerator()
        self.code_executor = CodeExecutor()
        self.paper_writer = PaperWriter()
    
    async def analyze_problem(self, task: Task) -> Dict[str, Any]:
        """分析数学建模问题"""
        return await self.problem_analyzer.analyze(task)
    
    async def generate_code(self, task: Task, analysis_result: Dict[str, Any]) -> str:
        """生成解决方案代码"""
        return await self.code_generator.generate(task, analysis_result)
    
    async def execute_code(self, code: str) -> Dict[str, Any]:
        """执行生成的代码"""
        return await self.code_executor.execute(code)
    
    async def generate_paper(
        self, 
        task: Task, 
        analysis_result: Dict[str, Any], 
        execution_result: Dict[str, Any]
    ) -> str:
        """生成数学建模论文"""
        return await self.paper_writer.write(task, analysis_result, execution_result)
    
    async def save_outputs(self, task: Task) -> Dict[str, str]:
        """保存输出文件"""
        output_files = {}
        
        # 保存论文
        if task.paper_content:
            paper_path = os.path.join(
                settings.OUTPUT_DIR, 
                f"task_{task.id}_paper.md"
            )
            with open(paper_path, 'w', encoding='utf-8') as f:
                f.write(task.paper_content)
            output_files['paper'] = paper_path
        
        # 保存代码
        if task.code_generated:
            code_path = os.path.join(
                settings.OUTPUT_DIR, 
                f"task_{task.id}_code.py"
            )
            with open(code_path, 'w', encoding='utf-8') as f:
                f.write(task.code_generated)
            output_files['code'] = code_path
        
        # 保存分析结果
        if task.analysis_result:
            analysis_path = os.path.join(
                settings.OUTPUT_DIR, 
                f"task_{task.id}_analysis.json"
            )
            with open(analysis_path, 'w', encoding='utf-8') as f:
                json.dump(task.analysis_result, f, ensure_ascii=False, indent=2)
            output_files['analysis'] = analysis_path
        
        return output_files
