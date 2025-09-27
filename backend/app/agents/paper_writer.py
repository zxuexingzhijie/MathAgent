"""
论文撰写Agent
"""

from typing import Dict, Any
from app.models.task import Task
from app.agents.base_agent import BaseAgent
from app.core.config import settings
from app.services.paper_formatter import PaperFormatter


class PaperWriter(BaseAgent):
    """论文撰写Agent - 生成数学建模论文"""
    
    def __init__(self):
        super().__init__()
        self.formatter = PaperFormatter()
    
    async def write(
        self, 
        task: Task, 
        analysis_result: Dict[str, Any], 
        execution_result: Dict[str, Any]
    ) -> str:
        """撰写数学建模论文"""
        prompt = self._build_paper_prompt(task, analysis_result, execution_result)
        
        response = await self.call_llm(
            prompt,
            model=settings.WRITING_MODEL,
            temperature=0.5,
            max_tokens=8000
        )
        
        # 格式化论文内容
        formatted_response = self.formatter.format_paper(
            response, 
            task.__dict__, 
            analysis_result, 
            execution_result
        )
        
        return formatted_response
    
    def _build_paper_prompt(
        self, 
        task: Task, 
        analysis_result: Dict[str, Any], 
        execution_result: Dict[str, Any]
    ) -> str:
        """构建论文撰写提示词"""
        prompt = f"""
你是一个专业的数学建模论文撰写专家。请根据以下信息撰写一份完整的数学建模论文。

问题信息:
标题: {task.title}
描述: {task.description}
类型: {task.task_type.value}

问题分析结果:
{self._format_analysis_result(analysis_result)}

代码执行结果:
{self._format_execution_result(execution_result)}

请按照标准的数学建模论文格式撰写，包括以下章节：

# {task.title}

## 摘要
简要概述问题、方法、结果和结论

## 1. 问题重述
详细描述问题背景、目标和约束条件

## 2. 问题分析
分析问题特点、难点和解决思路

## 3. 模型建立
详细描述数学模型，包括：
- 变量定义
- 目标函数
- 约束条件
- 模型假设

## 4. 模型求解
描述求解方法和算法

## 5. 结果分析
分析计算结果，包括：
- 数值结果
- 图表分析
- 敏感性分析

## 6. 模型评价
评价模型的优缺点和改进方向

## 7. 结论
总结主要发现和建议

## 参考文献
列出相关参考文献

请确保论文：
- 结构清晰，逻辑严密
- 数学表达准确
- 图表说明详细
- 结论有说服力
- 格式规范
"""
        return prompt
    
    def _format_analysis_result(self, analysis_result: Dict[str, Any]) -> str:
        """格式化分析结果"""
        formatted = []
        
        if 'objective' in analysis_result:
            formatted.append(f"目标: {analysis_result['objective']}")
        
        if 'problem_type' in analysis_result:
            formatted.append(f"问题类型: {analysis_result['problem_type']}")
        
        if 'variables' in analysis_result:
            formatted.append("变量定义:")
            for var in analysis_result['variables']:
                formatted.append(f"  - {var.get('name', '')}: {var.get('description', '')}")
        
        if 'constraints' in analysis_result:
            formatted.append("约束条件:")
            for constraint in analysis_result['constraints']:
                formatted.append(f"  - {constraint}")
        
        if 'algorithms' in analysis_result:
            formatted.append(f"推荐算法: {', '.join(analysis_result['algorithms'])}")
        
        return '\n'.join(formatted)
    
    def _format_execution_result(self, execution_result: Dict[str, Any]) -> str:
        """格式化执行结果"""
        if not execution_result.get('success', False):
            return f"代码执行失败: {execution_result.get('error', '未知错误')}"
        
        formatted = []
        formatted.append(f"执行状态: {'成功' if execution_result.get('success') else '失败'}")
        formatted.append(f"执行时间: {execution_result.get('execution_time', 0):.2f}秒")
        
        if execution_result.get('output'):
            formatted.append("输出结果:")
            formatted.append(execution_result['output'][:1000])  # 限制输出长度
        
        return '\n'.join(formatted)
