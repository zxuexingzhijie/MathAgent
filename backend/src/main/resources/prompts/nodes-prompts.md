# Nodes包提示词配置

## 数据收集提示词

基于以下问题分析结果，制定数据收集计划：

问题分析结果：
{problemAnalysis}

请制定详细的数据收集计划，包括：
1. 需要收集的数据类型
2. 数据来源建议
3. 数据收集方法
4. 数据预处理需求
5. 数据质量要求
6. 数据存储格式
7. 数据验证方法
8. 收集优先级和时间安排

请以JSON格式返回收集计划，包含以下字段：
- data_types: 数据类型列表
- data_sources: 数据来源建议
- collection_methods: 收集方法
- preprocessing_needs: 预处理需求
- quality_requirements: 质量要求
- storage_format: 存储格式
- validation_methods: 验证方法
- priority_order: 优先级排序
- estimated_time: 预估时间
- summary: 收集计划摘要

## 模型构建提示词

基于以下问题分析和数据收集结果，构建数学模型：

问题分析结果：
{problemAnalysis}

数据收集结果：
{collectedData}

请构建合适的数学模型，包括：
1. 模型类型选择（线性/非线性、确定性/随机性等）
2. 变量定义和关系
3. 目标函数或评价指标
4. 约束条件
5. 模型参数
6. 求解算法建议
7. 模型验证方法
8. 敏感性分析计划

请以JSON格式返回模型定义，包含以下字段：
- model_name: 模型名称
- model_type: 模型类型
- variables: 变量定义
- objective_function: 目标函数
- constraints: 约束条件
- parameters: 模型参数
- solving_algorithm: 求解算法
- validation_method: 验证方法
- sensitivity_analysis: 敏感性分析
- model_code: 模型代码（如适用）
- summary: 模型摘要

## 模型求解提示词

基于以下问题分析和模型定义，执行模型求解：

问题分析结果：
{problemAnalysis}

模型定义：
{modelDefinition}

请执行模型求解，包括：
1. 求解算法实现
2. 参数设置和优化
3. 求解过程记录
4. 结果验证
5. 敏感性分析
6. 结果解释
7. 误差分析
8. 求解性能评估

请以JSON格式返回求解结果，包含以下字段：
- algorithm_used: 使用的算法
- parameters: 求解参数
- solution_values: 求解结果值
- objective_value: 目标函数值
- solving_time: 求解时间
- convergence_info: 收敛信息
- validation_results: 验证结果
- sensitivity_analysis: 敏感性分析结果
- error_analysis: 误差分析
- performance_metrics: 性能指标
- interpretation: 结果解释
- summary: 求解摘要
