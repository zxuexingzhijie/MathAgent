"""
结果相关API端点
"""

from typing import List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.models.result import Result
from app.schemas.result import ResultResponse

router = APIRouter()


@router.get("/task/{task_id}", response_model=List[ResultResponse])
async def get_task_results(task_id: int, db: Session = Depends(get_db)):
    """获取任务的所有结果"""
    results = db.query(Result).filter(Result.task_id == task_id).all()
    return results


@router.get("/{result_id}", response_model=ResultResponse)
async def get_result(result_id: int, db: Session = Depends(get_db)):
    """获取单个结果详情"""
    result = db.query(Result).filter(Result.id == result_id).first()
    if not result:
        raise HTTPException(status_code=404, detail="Result not found")
    return result


@router.delete("/{result_id}")
async def delete_result(result_id: int, db: Session = Depends(get_db)):
    """删除结果"""
    result = db.query(Result).filter(Result.id == result_id).first()
    if not result:
        raise HTTPException(status_code=404, detail="Result not found")
    
    db.delete(result)
    db.commit()
    
    return {"message": "Result deleted successfully"}
