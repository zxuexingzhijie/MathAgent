package com.mathagent.graph;

import com.alibaba.cloud.ai.chat.ChatClient;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.dashscope.DashScopeChatModel;
import com.mathagent.agents.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.constant.Constant.*;

/**
 * 数学建模Multi-Agents Graph工作流配置
 * 集成建模手、代码手、论文手三个专业Agent
 */
@Slf4j
@Configuration
public class MathModelingGraphConfig {

    @Autowired
    private DashScopeChatModel chatModel;

    @Autowired
    private ModelingAgent modelingAgent;

    @Autowired
    private CodingAgent codingAgent;

    @Autowired
    private WritingAgent writingAgent;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public OverAllStateFactory stateFactory() {
        return () -> {
            OverAllState state = new OverAllState();
            // 注册状态键和策略
            state.registerKeyAndStrategy(INPUT_KEY, new ReplaceStrategy());
            state.registerKeyAndStrategy("task_id", new ReplaceStrategy());
            state.registerKeyAndStrategy("modeling_analysis", new ReplaceStrategy());
            state.registerKeyAndStrategy("model_definition", new ReplaceStrategy());
            state.registerKeyAndStrategy("variables", new ReplaceStrategy());
            state.registerKeyAndStrategy("constraints", new ReplaceStrategy());
            state.registerKeyAndStrategy("generated_code", new ReplaceStrategy());
            state.registerKeyAndStrategy("code_session_id", new ReplaceStrategy());
            state.registerKeyAndStrategy("coding_result", new ReplaceStrategy());
            state.registerKeyAndStrategy("debug_result", new ReplaceStrategy());
            state.registerKeyAndStrategy("generated_paper", new ReplaceStrategy());
            state.registerKeyAndStrategy("formatted_paper", new ReplaceStrategy());
            state.registerKeyAndStrategy("paper_abstract", new ReplaceStrategy());
            state.registerKeyAndStrategy("paper_keywords", new ReplaceStrategy());
            state.registerKeyAndStrategy("writing_result", new ReplaceStrategy());
            state.registerKeyAndStrategy(RESULT, new ReplaceStrategy());
            return state;
        };
    }

    @Bean
    public StateGraph mathModelingGraph(OverAllStateFactory stateFactory) throws GraphStateException {
        log.info("构建数学建模Multi-Agents Graph工作流...");
        
        StateGraph graph = new StateGraph("Math Modeling Multi-Agents Workflow", stateFactory)
                // 添加Multi-Agents节点
                .addNode("modeling_agent", node_async(modelingAgent))
                .addNode("coding_agent", node_async(codingAgent))
                .addNode("writing_agent", node_async(writingAgent))
                
                // 定义边（工作流序列）
                .addEdge(START, "modeling_agent")  // 开始节点
                .addEdge("modeling_agent", "coding_agent")
                .addEdge("coding_agent", "writing_agent")
                .addEdge("writing_agent", END);  // 结束节点

        log.info("数学建模Multi-Agents Graph工作流构建完成");
        return graph;
    }

    @Bean
    public CompiledGraph compiledMathModelingGraph(@Qualifier("mathModelingGraph") StateGraph stateGraph) 
            throws GraphStateException {
        log.info("编译数学建模Multi-Agents Graph工作流...");
        
        CompiledGraph compiledGraph = stateGraph.compile(CompileConfig.builder()
                .withLifecycleListener(new GraphObservationLifecycleListener())
                .build());
        
        // 设置最大迭代次数
        compiledGraph.setMaxIterations(100);
        
        log.info("数学建模Multi-Agents Graph工作流编译完成");
        return compiledGraph;
    }
}
