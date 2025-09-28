-- 数学建模任务表
CREATE TABLE IF NOT EXISTS math_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    status VARCHAR(50) NOT NULL COMMENT '任务状态',
    type VARCHAR(50) NOT NULL COMMENT '任务类型',
    problem_statement TEXT COMMENT '问题陈述',
    research_goals TEXT COMMENT '研究目标',
    data_requirements TEXT COMMENT '数据需求',
    model_constraints TEXT COMMENT '模型约束',
    expected_outputs TEXT COMMENT '预期输出',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    execution_time_ms BIGINT COMMENT '执行时间(毫秒)',
    graph_execution_id VARCHAR(100) COMMENT 'Graph执行ID',
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数学建模任务表';

-- 任务结果表
CREATE TABLE IF NOT EXISTS task_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    status VARCHAR(50) NOT NULL COMMENT '节点状态',
    input_data TEXT COMMENT '输入数据',
    output_data TEXT COMMENT '输出数据',
    error_message TEXT COMMENT '错误信息',
    execution_time_ms BIGINT COMMENT '执行时间(毫秒)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (task_id) REFERENCES math_tasks(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_node_name (node_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务结果表';

-- 任务日志表
CREATE TABLE IF NOT EXISTS task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    node_name VARCHAR(100) COMMENT '节点名称',
    level VARCHAR(20) NOT NULL COMMENT '日志级别',
    message TEXT NOT NULL COMMENT '日志消息',
    details TEXT COMMENT '详细信息',
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    FOREIGN KEY (task_id) REFERENCES math_tasks(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_level (level),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';
