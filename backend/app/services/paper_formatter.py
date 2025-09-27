"""
论文格式化服务
"""

import re
from typing import Dict, Any
from datetime import datetime


class PaperFormatter:
    """论文格式化服务类"""
    
    def __init__(self):
        self.template = self._get_default_template()
    
    def format_paper(
        self, 
        content: str, 
        task: Dict[str, Any],
        analysis_result: Dict[str, Any],
        execution_result: Dict[str, Any]
    ) -> str:
        """格式化论文内容"""
        # 替换模板变量
        formatted_content = self._replace_template_variables(
            content, task, analysis_result, execution_result
        )
        
        # 格式化数学公式
        formatted_content = self._format_math_formulas(formatted_content)
        
        # 格式化代码块
        formatted_content = self._format_code_blocks(formatted_content)
        
        # 格式化图表引用
        formatted_content = self._format_figure_references(formatted_content)
        
        return formatted_content
    
    def _get_default_template(self) -> str:
        """获取默认论文模板"""
        return """
# {title}

## 摘要

{abstract}

## 1. 问题重述

{problem_statement}

## 2. 问题分析

{analysis_content}

## 3. 模型建立

{model_content}

## 4. 模型求解

{solution_content}

## 5. 结果分析

{results_content}

## 6. 模型评价

{evaluation_content}

## 7. 结论

{conclusion}

## 参考文献

{references}

---
*生成时间: {generation_time}*
*任务ID: {task_id}*
"""
    
    def _replace_template_variables(
        self, 
        content: str,
        task: Dict[str, Any],
        analysis_result: Dict[str, Any],
        execution_result: Dict[str, Any]
    ) -> str:
        """替换模板变量"""
        variables = {
            'title': task.get('title', '数学建模问题'),
            'abstract': self._generate_abstract(task, analysis_result, execution_result),
            'problem_statement': task.get('description', ''),
            'analysis_content': self._format_analysis_content(analysis_result),
            'model_content': self._extract_model_content(content),
            'solution_content': self._extract_solution_content(content),
            'results_content': self._format_results_content(execution_result),
            'evaluation_content': self._extract_evaluation_content(content),
            'conclusion': self._extract_conclusion_content(content),
            'references': self._generate_references(),
            'generation_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            'task_id': task.get('id', 'N/A')
        }
        
        formatted_content = content
        for key, value in variables.items():
            formatted_content = formatted_content.replace(f'{{{key}}}', str(value))
        
        return formatted_content
    
    def _generate_abstract(
        self, 
        task: Dict[str, Any],
        analysis_result: Dict[str, Any],
        execution_result: Dict[str, Any]
    ) -> str:
        """生成摘要"""
        abstract_parts = []
        
        # 问题描述
        abstract_parts.append(f"本文研究了{task.get('title', '数学建模问题')}。")
        
        # 方法描述
        if analysis_result.get('solution_approach'):
            abstract_parts.append(f"采用{analysis_result['solution_approach']}的方法。")
        
        # 结果描述
        if execution_result.get('success'):
            abstract_parts.append("通过计算得到了有效的解决方案。")
        else:
            abstract_parts.append("在求解过程中遇到了一些技术挑战。")
        
        # 结论
        abstract_parts.append("研究结果对相关领域具有一定的参考价值。")
        
        return ' '.join(abstract_parts)
    
    def _format_analysis_content(self, analysis_result: Dict[str, Any]) -> str:
        """格式化分析内容"""
        content_parts = []
        
        if analysis_result.get('problem_type'):
            content_parts.append(f"**问题类型**: {analysis_result['problem_type']}")
        
        if analysis_result.get('objective'):
            content_parts.append(f"**问题目标**: {analysis_result['objective']}")
        
        if analysis_result.get('variables'):
            content_parts.append("**变量定义**:")
            for var in analysis_result['variables']:
                content_parts.append(f"- {var.get('name', '')}: {var.get('description', '')}")
        
        if analysis_result.get('constraints'):
            content_parts.append("**约束条件**:")
            for constraint in analysis_result['constraints']:
                content_parts.append(f"- {constraint}")
        
        return '\n\n'.join(content_parts)
    
    def _extract_model_content(self, content: str) -> str:
        """提取模型建立部分"""
        # 查找模型建立相关的内容
        model_pattern = r'## 3\. 模型建立(.*?)(?=## 4\.|$)'
        match = re.search(model_pattern, content, re.DOTALL)
        if match:
            return match.group(1).strip()
        return "模型建立内容待完善。"
    
    def _extract_solution_content(self, content: str) -> str:
        """提取模型求解部分"""
        solution_pattern = r'## 4\. 模型求解(.*?)(?=## 5\.|$)'
        match = re.search(solution_pattern, content, re.DOTALL)
        if match:
            return match.group(1).strip()
        return "模型求解方法待完善。"
    
    def _format_results_content(self, execution_result: Dict[str, Any]) -> str:
        """格式化结果分析内容"""
        if not execution_result.get('success'):
            return f"代码执行失败: {execution_result.get('error', '未知错误')}"
        
        content_parts = []
        
        if execution_result.get('output'):
            content_parts.append("**计算结果**:")
            content_parts.append("```")
            content_parts.append(execution_result['output'][:1000])  # 限制长度
            content_parts.append("```")
        
        if execution_result.get('execution_time'):
            content_parts.append(f"**执行时间**: {execution_result['execution_time']:.2f}秒")
        
        return '\n\n'.join(content_parts)
    
    def _extract_evaluation_content(self, content: str) -> str:
        """提取模型评价部分"""
        evaluation_pattern = r'## 6\. 模型评价(.*?)(?=## 7\.|$)'
        match = re.search(evaluation_pattern, content, re.DOTALL)
        if match:
            return match.group(1).strip()
        return "模型评价内容待完善。"
    
    def _extract_conclusion_content(self, content: str) -> str:
        """提取结论部分"""
        conclusion_pattern = r'## 7\. 结论(.*?)(?=## 参考文献|$)'
        match = re.search(conclusion_pattern, content, re.DOTALL)
        if match:
            return match.group(1).strip()
        return "结论内容待完善。"
    
    def _generate_references(self) -> str:
        """生成参考文献"""
        references = [
            "1. 相关学术文献1",
            "2. 相关学术文献2", 
            "3. 相关学术文献3"
        ]
        return '\n'.join(references)
    
    def _format_math_formulas(self, content: str) -> str:
        """格式化数学公式"""
        # 将LaTeX格式的数学公式转换为Markdown格式
        # $...$ -> $...$
        # $$...$$ -> $$...$$
        return content
    
    def _format_code_blocks(self, content: str) -> str:
        """格式化代码块"""
        # 确保代码块有正确的语言标识
        code_pattern = r'```(\w+)?\n(.*?)```'
        
        def replace_code_block(match):
            lang = match.group(1) or 'python'
            code = match.group(2)
            return f'```{lang}\n{code}\n```'
        
        return re.sub(code_pattern, replace_code_block, content, flags=re.DOTALL)
    
    def _format_figure_references(self, content: str) -> str:
        """格式化图表引用"""
        # 处理图表引用，如 [图1], [表1] 等
        figure_pattern = r'\[图(\d+)\]'
        table_pattern = r'\[表(\d+)\]'
        
        content = re.sub(figure_pattern, r'![图\1](图\1.png)', content)
        content = re.sub(table_pattern, r'![表\1](表\1.png)', content)
        
        return content
