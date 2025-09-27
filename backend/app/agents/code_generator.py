"""
代码生成Agent
"""

from typing import Dict, Any
from app.models.task import Task
from app.agents.base_agent import BaseAgent
from app.core.config import settings


class CodeGenerator(BaseAgent):
    """代码生成Agent - 根据问题分析生成Python代码"""
    
    async def generate(self, task: Task, analysis_result: Dict[str, Any]) -> str:
        """生成解决方案代码"""
        prompt = self._build_code_prompt(task, analysis_result)
        
        response = await self.call_llm(
            prompt,
            model=settings.CODE_MODEL,
            temperature=0.2,
            max_tokens=6000
        )
        
        return response
    
    def _build_code_prompt(self, task: Task, analysis_result: Dict[str, Any]) -> str:
        """构建代码生成提示词"""
        prompt = f"""
你是一个专业的Python数据科学和数学建模专家。请根据问题分析结果生成完整的Python代码解决方案。

问题信息:
标题: {task.title}
描述: {task.description}
类型: {task.task_type.value}

分析结果:
目标: {analysis_result.get('objective', '未知')}
问题类型: {analysis_result.get('problem_type', '未知')}
推荐算法: {', '.join(analysis_result.get('algorithms', []))}
评估指标: {', '.join(analysis_result.get('evaluation_metrics', []))}

约束条件:
{task.constraints or "无特殊约束"}

请生成完整的Python代码，包括：

1. 必要的库导入
2. 数据加载和预处理
3. 模型构建和训练
4. 结果分析和可视化
5. 结果输出和保存

代码要求：
- 使用标准的Python数据科学库（pandas, numpy, matplotlib, seaborn, scikit-learn等）
- 包含详细的注释说明
- 代码结构清晰，函数化设计
- 包含错误处理
- 生成图表和可视化
- 输出结果到文件

请直接返回Python代码，不要包含其他说明文字。
"""
        return prompt
