package com.mathagent.mapper;

import com.mathagent.model.MathTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数学建模任务Mapper
 */
@Mapper
public interface MathTaskMapper {
    
    /**
     * 插入任务
     */
    @Insert("""
        INSERT INTO math_tasks (title, description, status, type, problem_statement, 
                              research_goals, data_requirements, model_constraints, 
                              expected_outputs, created_at, updated_at)
        VALUES (#{title}, #{description}, #{status}, #{type}, #{problemStatement}, 
                #{researchGoals}, #{dataRequirements}, #{modelConstraints}, 
                #{expectedOutputs}, #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MathTask task);
    
    /**
     * 根据ID查询任务
     */
    @Select("SELECT * FROM math_tasks WHERE id = #{id}")
    MathTask selectById(Long id);
    
    /**
     * 查询所有任务
     */
    @Select("SELECT * FROM math_tasks ORDER BY created_at DESC")
    List<MathTask> selectAll();
    
    /**
     * 根据状态查询任务
     */
    @Select("SELECT * FROM math_tasks WHERE status = #{status} ORDER BY created_at DESC")
    List<MathTask> selectByStatus(MathTask.TaskStatus status);
    
    /**
     * 根据类型查询任务
     */
    @Select("SELECT * FROM math_tasks WHERE type = #{type} ORDER BY created_at DESC")
    List<MathTask> selectByType(MathTask.TaskType type);
    
    /**
     * 根据标题模糊查询
     */
    @Select("SELECT * FROM math_tasks WHERE title LIKE CONCAT('%', #{title}, '%') ORDER BY created_at DESC")
    List<MathTask> selectByTitleContaining(String title);
    
    /**
     * 更新任务
     */
    @Update("""
        UPDATE math_tasks SET 
            title = #{title},
            description = #{description},
            status = #{status},
            type = #{type},
            problem_statement = #{problemStatement},
            research_goals = #{researchGoals},
            data_requirements = #{dataRequirements},
            model_constraints = #{modelConstraints},
            expected_outputs = #{expectedOutputs},
            started_at = #{startedAt},
            completed_at = #{completedAt},
            execution_time_ms = #{executionTimeMs},
            graph_execution_id = #{graphExecutionId},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    int update(MathTask task);
    
    /**
     * 更新任务状态
     */
    @Update("UPDATE math_tasks SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") MathTask.TaskStatus status);
    
    /**
     * 删除任务
     */
    @Delete("DELETE FROM math_tasks WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 统计各状态的任务数量
     */
    @Select("SELECT status, COUNT(*) as count FROM math_tasks GROUP BY status")
    List<Object[]> countByStatus();
}
