package com.mathmodel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Math Model Agent Application
 * 
 * @author MathModelAgent
 * @version 1.0.0
 */
@Slf4j
@EnableAsync
@EnableAspectJAutoProxy
@SpringBootApplication
public class MathModelAgentApplication {

    public static void main(String[] args) {
        printBanner();
        SpringApplication.run(MathModelAgentApplication.class, args);
        log.info("🎉 Math Model Agent started successfully!");
    }
    
    private static void printBanner() {
        String banner = """
                
                ███╗   ███╗ █████╗ ████████╗██╗  ██╗    ███╗   ███╗ ██████╗ ██████╗ ███████╗██╗         █████╗  ██████╗ ███████╗███╗   ██╗████████╗
                ████╗ ████║██╔══██╗╚══██╔══╝██║  ██║    ████╗ ████║██╔═══██╗██╔══██╗██╔════╝██║        ██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝
                ██╔████╔██║███████║   ██║   ███████║    ██╔████╔██║██║   ██║██║  ██║█████╗  ██║        ███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   
                ██║╚██╔╝██║██╔══██║   ██║   ██╔══██║    ██║╚██╔╝██║██║   ██║██║  ██║██╔══╝  ██║        ██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   
                ██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║    ██║ ╚═╝ ██║╚██████╔╝██████╔╝███████╗███████╗    ██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   
                ╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝    ╚═╝     ╚═╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝    ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   
                
                🤖 Mathematical Modeling Agent - Java Edition
                🔥 Powered by Spring AI Alibaba
                """;
        System.out.println(banner);
    }
}
