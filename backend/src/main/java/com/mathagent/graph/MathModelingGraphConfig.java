package com.mathagent.graph;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.observation.GraphObservationLifecycleListener;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import com.mathagent.agents.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.OverAllState.DEFAULT_INPUT_KEY;

/**
 * 数学建模Multi-Agents Graph工作流配置 集成建模手、代码手、论文手三个专业Agent
 */
@Slf4j
@Configuration
public class MathModelingGraphConfig {

	@Autowired
	@Qualifier("dashScopeChatModel")
	private DashScopeChatModel chatModel;

	@Autowired
	private ModelingAgent modelingAgent;

	@Autowired
	private CodingAgent codingAgent;

	@Autowired
	private WritingAgent writingAgent;

	@Bean
	public ChatClient chatClient(@Qualifier("dashScopeChatModel") DashScopeChatModel dashScopeChatModel) {
		return ChatClient.builder(dashScopeChatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
	}

	@Bean
	public KeyStrategyFactory keyStrategyFactory() {
		return new KeyStrategyFactoryBuilder().addStrategy(DEFAULT_INPUT_KEY, KeyStrategy.REPLACE)
			.addStrategy("task_id", KeyStrategy.REPLACE)
			.addStrategy("modeling_analysis", KeyStrategy.REPLACE)
			.addStrategy("model_definition", KeyStrategy.REPLACE)
			.addStrategy("variables", KeyStrategy.REPLACE)
			.addStrategy("constraints", KeyStrategy.REPLACE)
			.addStrategy("generated_code", KeyStrategy.REPLACE)
			.addStrategy("code_session_id", KeyStrategy.REPLACE)
			.addStrategy("coding_result", KeyStrategy.REPLACE)
			.addStrategy("debug_result", KeyStrategy.REPLACE)
			.addStrategy("generated_paper", KeyStrategy.REPLACE)
			.addStrategy("formatted_paper", KeyStrategy.REPLACE)
			.addStrategy("paper_abstract", KeyStrategy.REPLACE)
			.addStrategy("paper_keywords", KeyStrategy.REPLACE)
			.addStrategy("writing_result", KeyStrategy.REPLACE)
			.addStrategy("result", KeyStrategy.REPLACE)
			.build();
	}

	@Bean
	public StateGraph mathModelingGraph(KeyStrategyFactory keyStrategyFactory) throws GraphStateException {
		log.info("构建数学建模Multi-Agents Graph工作流...");

		StateGraph graph = new StateGraph("Math Modeling Multi-Agents Workflow", keyStrategyFactory)
			// 添加Multi-Agents节点
			.addNode("modeling_agent", node_async(modelingAgent))
			.addNode("coding_agent", node_async(codingAgent))
			.addNode("writing_agent", node_async(writingAgent))

			// 定义边（工作流序列）
			.addEdge(START, "modeling_agent") // 开始节点
			.addEdge("modeling_agent", "coding_agent")
			.addEdge("coding_agent", "writing_agent")
			.addEdge("writing_agent", END); // 结束节点

		log.info("数学建模Multi-Agents Graph工作流构建完成");
		return graph;
	}

	@Bean
	public CompiledGraph compiledMathModelingGraph(@Qualifier("mathModelingGraph") StateGraph stateGraph)
			throws GraphStateException {
		log.info("编译数学建模Multi-Agents Graph工作流...");

		CompiledGraph compiledGraph = stateGraph.compile(CompileConfig.builder().build());

		// 设置最大迭代次数
		compiledGraph.setMaxIterations(100);

		log.info("数学建模Multi-Agents Graph工作流编译完成");
		return compiledGraph;
	}

}
