"""
任务服务
"""

from datetime import datetime
from typing import List, Optional
from sqlalchemy.orm import Session

from app.models.task import Task, TaskStatus
from app.schemas.task import TaskCreate, TaskUpdate
from app.services.agent_service import AgentService
from app.services.websocket_manager import WebSocketManager


class TaskService:
    """任务服务类"""
    
    def __init__(self, db: Session):
        self.db = db
        self.agent_service = AgentService()
        self.websocket_manager = WebSocketManager()
    
    async def create_task(self, task_data: TaskCreate) -> Task:
        """创建新任务"""
        task = Task(
            title=task_data.title,
            description=task_data.description,
            task_type=task_data.task_type,
            problem_statement=task_data.problem_statement,
            constraints=task_data.constraints,
            input_data=task_data.input_data,
            config=task_data.config,
            status=TaskStatus.PENDING
        )
        
        self.db.add(task)
        self.db.commit()
        self.db.refresh(task)
        
        return task
    
    async def get_task(self, task_id: int) -> Optional[Task]:
        """获取任务"""
        return self.db.query(Task).filter(Task.id == task_id).first()
    
    async def get_tasks(
        self,
        skip: int = 0,
        limit: int = 100,
        status: Optional[TaskStatus] = None
    ) -> List[Task]:
        """获取任务列表"""
        query = self.db.query(Task)
        if status:
            query = query.filter(Task.status == status)
        return query.offset(skip).limit(limit).all()
    
    async def update_task(self, task_id: int, task_data: TaskUpdate) -> Optional[Task]:
        """更新任务"""
        task = await self.get_task(task_id)
        if not task:
            return None
        
        for field, value in task_data.dict(exclude_unset=True).items():
            setattr(task, field, value)
        
        self.db.commit()
        self.db.refresh(task)
        
        return task
    
    async def delete_task(self, task_id: int) -> bool:
        """删除任务"""
        task = await self.get_task(task_id)
        if not task:
            return False
        
        self.db.delete(task)
        self.db.commit()
        return True
    
    async def cancel_task(self, task_id: int) -> bool:
        """取消任务"""
        task = await self.get_task(task_id)
        if not task:
            return False
        
        task.status = TaskStatus.CANCELLED
        self.db.commit()
        
        # 通知WebSocket客户端
        await self.websocket_manager.broadcast_status(
            str(task_id), 
            "cancelled", 
            {"message": "任务已取消"}
        )
        
        return True
    
    async def process_task(self, task_id: int):
        """处理任务"""
        task = await self.get_task(task_id)
        if not task:
            return
        
        try:
            # 更新任务状态为运行中
            task.status = TaskStatus.RUNNING
            task.started_at = datetime.utcnow()
            self.db.commit()
            
            # 通知WebSocket客户端
            await self.websocket_manager.broadcast_status(
                str(task_id), 
                "running", 
                {"message": "开始处理任务"}
            )
            
            # 执行Agent处理流程
            await self._execute_agent_pipeline(task)
            
            # 更新任务状态为完成
            task.status = TaskStatus.COMPLETED
            task.completed_at = datetime.utcnow()
            self.db.commit()
            
            # 通知WebSocket客户端
            await self.websocket_manager.broadcast_status(
                str(task_id), 
                "completed", 
                {"message": "任务处理完成"}
            )
            
        except Exception as e:
            # 更新任务状态为失败
            task.status = TaskStatus.FAILED
            task.error_message = str(e)
            self.db.commit()
            
            # 通知WebSocket客户端
            await self.websocket_manager.broadcast_error(
                str(task_id), 
                f"任务处理失败: {str(e)}"
            )
    
    async def _execute_agent_pipeline(self, task: Task):
        """执行Agent处理流程"""
        # 1. 问题分析
        await self.websocket_manager.broadcast_progress(
            str(task.id), 10, "正在分析问题..."
        )
        analysis_result = await self.agent_service.analyze_problem(task)
        task.analysis_result = analysis_result
        self.db.commit()
        
        # 2. 代码生成
        await self.websocket_manager.broadcast_progress(
            str(task.id), 30, "正在生成代码..."
        )
        code_generated = await self.agent_service.generate_code(task, analysis_result)
        task.code_generated = code_generated
        self.db.commit()
        
        # 3. 代码执行
        await self.websocket_manager.broadcast_progress(
            str(task.id), 60, "正在执行代码..."
        )
        execution_result = await self.agent_service.execute_code(code_generated)
        task.execution_result = execution_result
        self.db.commit()
        
        # 4. 论文生成
        await self.websocket_manager.broadcast_progress(
            str(task.id), 80, "正在生成论文..."
        )
        paper_content = await self.agent_service.generate_paper(
            task, analysis_result, execution_result
        )
        task.paper_content = paper_content
        self.db.commit()
        
        # 5. 保存输出文件
        await self.websocket_manager.broadcast_progress(
            str(task.id), 90, "正在保存结果..."
        )
        output_files = await self.agent_service.save_outputs(task)
        task.output_files = output_files
        self.db.commit()
        
        await self.websocket_manager.broadcast_progress(
            str(task.id), 100, "任务完成！"
        )
