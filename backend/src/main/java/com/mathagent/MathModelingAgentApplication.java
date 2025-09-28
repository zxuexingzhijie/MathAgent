package com.mathagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 数学建模DeepResearch Agent应用启动类
 * 
 * @author Makoto
 */
@SpringBootApplication
@EnableAsync
public class MathModelingAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MathModelingAgentApplication.class, args);
	}

}
