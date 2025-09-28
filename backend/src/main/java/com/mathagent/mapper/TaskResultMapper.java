package com.mathagent.mapper;

import com.mathagent.model.TaskResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务结果Mapper
 */
@Mapper
public interface TaskResultMapper {

	/**
	 * 插入任务结果
	 */
	@Insert("""
			INSERT INTO task_results (task_id, node_name, status, input_data, output_data,
			                        error_message, execution_time_ms, created_at, updated_at)
			VALUES (#{taskId}, #{nodeName}, #{status}, #{inputData}, #{outputData},
			        #{errorMessage}, #{executionTimeMs}, #{createdAt}, #{updatedAt})
			""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(TaskResult result);

	/**
	 * 根据ID查询结果
	 */
	@Select("SELECT * FROM task_results WHERE id = #{id}")
	TaskResult selectById(Long id);

	/**
	 * 根据任务ID查询所有结果
	 */
	@Select("SELECT * FROM task_results WHERE task_id = #{taskId} ORDER BY created_at ASC")
	List<TaskResult> selectByTaskId(Long taskId);

	/**
	 * 根据任务ID和节点名称查询结果
	 */
	@Select("SELECT * FROM task_results WHERE task_id = #{taskId} AND node_name = #{nodeName}")
	TaskResult selectByTaskIdAndNodeName(@Param("taskId") Long taskId, @Param("nodeName") String nodeName);

	/**
	 * 更新任务结果
	 */
	@Update("""
			UPDATE task_results SET
			    status = #{status},
			    input_data = #{inputData},
			    output_data = #{outputData},
			    error_message = #{errorMessage},
			    execution_time_ms = #{executionTimeMs},
			    updated_at = #{updatedAt}
			WHERE id = #{id}
			""")
	int update(TaskResult result);

	/**
	 * 删除任务结果
	 */
	@Delete("DELETE FROM task_results WHERE id = #{id}")
	int deleteById(Long id);

	/**
	 * 根据任务ID删除所有结果
	 */
	@Delete("DELETE FROM task_results WHERE task_id = #{taskId}")
	int deleteByTaskId(Long taskId);

}
