"""
API路由配置
"""

from fastapi import APIRouter

from app.api.endpoints import tasks, users, results, settings

# 创建主路由器
api_router = APIRouter()

# 注册子路由
api_router.include_router(
    tasks.router,
    prefix="/tasks",
    tags=["tasks"]
)

api_router.include_router(
    users.router,
    prefix="/users",
    tags=["users"]
)

api_router.include_router(
    results.router,
    prefix="/results",
    tags=["results"]
)

api_router.include_router(
    settings.router,
    prefix="/settings",
    tags=["settings"]
)
