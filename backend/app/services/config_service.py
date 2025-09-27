"""
配置管理服务
"""

import os
import json
from typing import Dict, Any, Optional
from pathlib import Path

from app.core.config import settings


class ConfigService:
    """配置管理服务类"""
    
    def __init__(self):
        self.config_file = Path(settings.OUTPUT_DIR) / "config.json"
        self.config_file.parent.mkdir(exist_ok=True)
    
    def load_config(self) -> Dict[str, Any]:
        """加载配置文件"""
        if self.config_file.exists():
            try:
                with open(self.config_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except Exception as e:
                print(f"加载配置文件失败: {e}")
                return {}
        return {}
    
    def save_config(self, config: Dict[str, Any]) -> bool:
        """保存配置文件"""
        try:
            with open(self.config_file, 'w', encoding='utf-8') as f:
                json.dump(config, f, ensure_ascii=False, indent=2)
            return True
        except Exception as e:
            print(f"保存配置文件失败: {e}")
            return False
    
    def update_config(self, updates: Dict[str, Any]) -> bool:
        """更新配置"""
        config = self.load_config()
        config.update(updates)
        return self.save_config(config)
    
    def get_config_value(self, key: str, default: Any = None) -> Any:
        """获取配置值"""
        config = self.load_config()
        return config.get(key, default)
    
    def set_config_value(self, key: str, value: Any) -> bool:
        """设置配置值"""
        config = self.load_config()
        config[key] = value
        return self.save_config(config)
    
    def reset_config(self) -> bool:
        """重置配置"""
        default_config = self._get_default_config()
        return self.save_config(default_config)
    
    def _get_default_config(self) -> Dict[str, Any]:
        """获取默认配置"""
        return {
            "api_keys": {
                "openai": "",
                "anthropic": ""
            },
            "models": {
                "default": "gpt-4",
                "code": "gpt-4",
                "writing": "gpt-4",
                "analysis": "gpt-4"
            },
            "task_settings": {
                "timeout": 3600,
                "max_concurrent": 5
            },
            "ui_settings": {
                "theme": "light",
                "language": "zh-CN"
            }
        }
    
    def validate_config(self, config: Dict[str, Any]) -> Dict[str, Any]:
        """验证配置"""
        errors = {}
        
        # 验证API密钥
        api_keys = config.get("api_keys", {})
        if not api_keys.get("openai") and not api_keys.get("anthropic"):
            errors["api_keys"] = "至少需要配置一个API密钥"
        
        # 验证模型配置
        models = config.get("models", {})
        required_models = ["default", "code", "writing", "analysis"]
        for model_key in required_models:
            if not models.get(model_key):
                errors[f"models.{model_key}"] = f"{model_key}模型不能为空"
        
        # 验证任务设置
        task_settings = config.get("task_settings", {})
        if task_settings.get("timeout", 0) < 300:
            errors["task_settings.timeout"] = "任务超时时间不能少于300秒"
        
        if task_settings.get("max_concurrent", 0) < 1:
            errors["task_settings.max_concurrent"] = "最大并发任务数不能少于1"
        
        return errors
