package com.ruoyi.agent.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步线程池配置
 *
 * @author ruoyi
 */
@EnableAsync
@Configuration
public class AsyncConfig
{
    @Bean("kbIngestionExecutor")
    public Executor kbIngestionExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("kb-ingest-");
        executor.initialize();
        return executor;
    }
}
