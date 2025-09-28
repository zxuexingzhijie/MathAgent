package com.mathagent.mapper;

import com.mathagent.model.TaskLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务日志Mapper
 */
@Mapper
public interface TaskLogMapper {
    
    /**
     * 插入任务日志
     */
    @Insert("""
        INSERT INTO task_logs (task_id, node_name, level, message, details, timestamp)
        VALUES (#{taskId}, #{nodeName}, #{level}, #{message}, #{details}, #{timestamp})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskLog log);
    
    /**
     * 根据ID查询日志
     */
    @Select("SELECT * FROM task_logs WHERE id = #{id}")
    TaskLog selectById(Long id);
    
    /**
     * 根据任务ID查询所有日志
     */
    @Select("SELECT * FROM task_logs WHERE task_id = #{taskId} ORDER BY timestamp ASC")
    List<TaskLog> selectByTaskId(Long taskId);
    
    /**
     * 根据任务ID和节点名称查询日志
     */
    @Select("SELECT * FROM task_logs WHERE task_id = #{taskId} AND node_name = #{nodeName} ORDER BY timestamp ASC")
    List<TaskLog> selectByTaskIdAndNodeName(@Param("taskId") Long taskId, @Param("nodeName") String nodeName);
    
    /**
     * 根据日志级别查询日志
     */
    @Select("SELECT * FROM task_logs WHERE level = #{level} ORDER BY timestamp DESC")
    List<TaskLog> selectByLevel(TaskLog.LogLevel level);
    
    /**
     * 删除任务日志
     */
    @Delete("DELETE FROM task_logs WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 根据任务ID删除所有日志
     */
    @Delete("DELETE FROM task_logs WHERE task_id = #{taskId}")
    int deleteByTaskId(Long taskId);
}
