-- DeepResearchAgent MySQL数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS deepresearch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE deepresearch;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    hashed_password VARCHAR(255) NOT NULL COMMENT '加密密码',
    full_name VARCHAR(100) COMMENT '全名',
    organization VARCHAR(100) COMMENT '组织',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否验证',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login TIMESTAMP NULL COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建任务表
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '任务标题',
    description TEXT NOT NULL COMMENT '任务描述',
    task_type ENUM('math_modeling', 'data_analysis', 'optimization', 'simulation') NOT NULL COMMENT '任务类型',
    status ENUM('pending', 'running', 'completed', 'failed', 'cancelled') DEFAULT 'pending' COMMENT '任务状态',
    input_data JSON COMMENT '输入数据',
    problem_statement TEXT COMMENT '问题陈述',
    constraints TEXT COMMENT '约束条件',
    analysis_result JSON COMMENT '分析结果',
    code_generated TEXT COMMENT '生成的代码',
    execution_result JSON COMMENT '执行结果',
    paper_content LONGTEXT COMMENT '论文内容',
    output_files JSON COMMENT '输出文件路径',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    user_id INT COMMENT '用户ID',
    config JSON COMMENT '任务配置',
    error_message TEXT COMMENT '错误信息',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_task_type (task_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 创建结果表
CREATE TABLE IF NOT EXISTS results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL COMMENT '任务ID',
    result_type VARCHAR(50) NOT NULL COMMENT '结果类型',
    title VARCHAR(255) COMMENT '结果标题',
    content LONGTEXT COMMENT '结果内容',
    data JSON COMMENT '结果数据',
    metadata JSON COMMENT '元数据',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_result_type (result_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结果表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description TEXT COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('default_model', 'gpt-4', '默认使用的AI模型'),
('code_model', 'gpt-4', '代码生成使用的AI模型'),
('writing_model', 'gpt-4', '论文撰写使用的AI模型'),
('analysis_model', 'gpt-4', '问题分析使用的AI模型'),
('task_timeout', '3600', '任务超时时间（秒）'),
('max_concurrent_tasks', '5', '最大并发任务数'),
('upload_max_size', '10485760', '上传文件最大大小（字节）'),
('output_retention_days', '30', '输出文件保留天数')
ON DUPLICATE KEY UPDATE 
config_value = VALUES(config_value),
updated_at = CURRENT_TIMESTAMP;

-- 创建索引优化查询性能
CREATE INDEX idx_tasks_user_status ON tasks(user_id, status);
CREATE INDEX idx_tasks_type_status ON tasks(task_type, status);
CREATE INDEX idx_tasks_created_status ON tasks(created_at, status);
CREATE INDEX idx_results_task_type ON results(task_id, result_type);

-- 显示创建结果
SHOW TABLES;
SELECT 'Database setup completed successfully!' as message;
