"""
Jupyter服务 - 提供代码执行环境
"""

import os
import json
import tempfile
import asyncio
from typing import Dict, Any, Optional
import subprocess
import sys
from pathlib import Path

from app.core.config import settings


class JupyterService:
    """Jupyter服务类 - 管理代码执行环境"""
    
    def __init__(self):
        self.kernel_dir = Path(settings.OUTPUT_DIR) / "kernels"
        self.kernel_dir.mkdir(exist_ok=True)
        self.active_kernels = {}
    
    async def execute_code(
        self, 
        code: str, 
        kernel_id: Optional[str] = None,
        timeout: int = 300
    ) -> Dict[str, Any]:
        """执行Python代码"""
        try:
            # 创建临时文件
            with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
                f.write(code)
                temp_file = f.name
            
            # 执行代码
            result = await self._run_python_file(temp_file, timeout)
            
            # 清理临时文件
            os.unlink(temp_file)
            
            return result
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "output": "",
                "execution_time": 0
            }
    
    async def _run_python_file(self, file_path: str, timeout: int) -> Dict[str, Any]:
        """运行Python文件"""
        import time
        start_time = time.time()
        
        try:
            # 使用subprocess运行代码
            process = await asyncio.create_subprocess_exec(
                sys.executable, file_path,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=os.path.dirname(file_path)
            )
            
            try:
                stdout, stderr = await asyncio.wait_for(
                    process.communicate(), 
                    timeout=timeout
                )
            except asyncio.TimeoutError:
                process.kill()
                await process.wait()
                return {
                    "success": False,
                    "error": f"代码执行超时（{timeout}秒）",
                    "output": "",
                    "execution_time": timeout
                }
            
            end_time = time.time()
            execution_time = end_time - start_time
            
            return {
                "success": process.returncode == 0,
                "output": stdout.decode('utf-8'),
                "error": stderr.decode('utf-8') if stderr else "",
                "return_code": process.returncode,
                "execution_time": execution_time
            }
            
        except Exception as e:
            end_time = time.time()
            execution_time = end_time - start_time
            
            return {
                "success": False,
                "error": str(e),
                "output": "",
                "execution_time": execution_time
            }
    
    async def execute_notebook(self, notebook_path: str) -> Dict[str, Any]:
        """执行Jupyter Notebook"""
        try:
            # 使用nbconvert执行notebook
            cmd = [
                sys.executable, "-m", "jupyter", "nbconvert",
                "--execute",
                "--to", "notebook",
                "--output-dir", str(self.kernel_dir),
                notebook_path
            ]
            
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            
            stdout, stderr = await process.communicate()
            
            return {
                "success": process.returncode == 0,
                "output": stdout.decode('utf-8'),
                "error": stderr.decode('utf-8') if stderr else "",
                "return_code": process.returncode
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "output": "",
                "return_code": -1
            }
    
    async def create_notebook(self, code: str, task_id: int) -> str:
        """创建Jupyter Notebook文件"""
        notebook_content = {
            "cells": [
                {
                    "cell_type": "markdown",
                    "metadata": {},
                    "source": [
                        f"# 任务 {task_id} - 数学建模代码\n",
                        f"生成时间: {self._get_current_time()}\n"
                    ]
                },
                {
                    "cell_type": "code",
                    "execution_count": None,
                    "metadata": {},
                    "outputs": [],
                    "source": code.split('\n')
                }
            ],
            "metadata": {
                "kernelspec": {
                    "display_name": "Python 3",
                    "language": "python",
                    "name": "python3"
                },
                "language_info": {
                    "name": "python",
                    "version": "3.9.0"
                }
            },
            "nbformat": 4,
            "nbformat_minor": 4
        }
        
        notebook_path = self.kernel_dir / f"task_{task_id}_notebook.ipynb"
        
        with open(notebook_path, 'w', encoding='utf-8') as f:
            json.dump(notebook_content, f, ensure_ascii=False, indent=2)
        
        return str(notebook_path)
    
    def _get_current_time(self) -> str:
        """获取当前时间字符串"""
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    async def cleanup_kernels(self):
        """清理过期的内核"""
        # 这里可以实现内核清理逻辑
        pass
