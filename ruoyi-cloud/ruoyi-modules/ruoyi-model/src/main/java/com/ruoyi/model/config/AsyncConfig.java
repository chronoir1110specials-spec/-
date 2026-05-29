package com.ruoyi.model.config;

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
    @Bean("modelLogExecutor")
    public Executor modelLogExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("model-log-");
        executor.initialize();
        return executor;
    }
}
