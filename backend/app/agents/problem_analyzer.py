"""
问题分析Agent
"""

import json
from typing import Dict, Any
from app.models.task import Task
from app.agents.base_agent import BaseAgent


class ProblemAnalyzer(BaseAgent):
    """问题分析Agent - 分析数学建模问题"""
    
    async def analyze(self, task: Task) -> Dict[str, Any]:
        """分析数学建模问题"""
        prompt = self._build_analysis_prompt(task)
        
        response = await self.call_llm(
            prompt, 
            model=settings.ANALYSIS_MODEL,
            temperature=0.3
        )
        
        # 解析响应
        try:
            analysis_result = json.loads(response)
        except json.JSONDecodeError:
            # 如果JSON解析失败，尝试提取结构化信息
            analysis_result = self._parse_text_response(response)
        
        return analysis_result
    
    def _build_analysis_prompt(self, task: Task) -> str:
        """构建分析提示词"""
        prompt = f"""
你是一个专业的数学建模问题分析专家。请分析以下数学建模问题，并返回JSON格式的分析结果。

问题标题: {task.title}
问题描述: {task.description}
问题类型: {task.task_type.value}

约束条件:
{task.constraints or "无特殊约束"}

输入数据:
{json.dumps(task.input_data, ensure_ascii=False, indent=2) if task.input_data else "无输入数据"}

请按照以下JSON格式返回分析结果：
{{
    "problem_type": "问题类型（如：优化问题、预测问题、分类问题等）",
    "objective": "问题目标",
    "variables": [
        {{"name": "变量名", "type": "变量类型", "description": "变量描述"}}
    ],
    "constraints": [
        "约束条件1",
        "约束条件2"
    ],
    "data_requirements": [
        "所需数据类型1",
        "所需数据类型2"
    ],
    "solution_approach": "建议的解决方案",
    "algorithms": [
        "推荐算法1",
        "推荐算法2"
    ],
    "evaluation_metrics": [
        "评估指标1",
        "评估指标2"
    ],
    "complexity": "问题复杂度评估",
    "notes": "其他重要说明"
}}

请确保返回的是有效的JSON格式。
"""
        return prompt
    
    def _parse_text_response(self, response: str) -> Dict[str, Any]:
        """解析文本响应为结构化数据"""
        return {
            "problem_type": "未知",
            "objective": "待分析",
            "variables": [],
            "constraints": [],
            "data_requirements": [],
            "solution_approach": response[:500],  # 取前500字符作为方案
            "algorithms": [],
            "evaluation_metrics": [],
            "complexity": "中等",
            "notes": "自动解析的文本响应"
        }
