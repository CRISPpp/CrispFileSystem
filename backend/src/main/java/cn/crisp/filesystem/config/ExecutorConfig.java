package cn.crisp.filesystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);

    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;
    @Value("${async.executor.thread.keep_alive_seconds}")
    private int keepAliveSeconds;
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix;

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        logger.info("线程池信息");
        logger.info("corePoolSize: " + Integer.toString(corePoolSize));
        logger.info("maxPoolSize: " + Integer.toString(maxPoolSize));
        logger.info("queueCapacity: " + Integer.toString(queueCapacity));
        logger.info("keepAliveSeconds: " + Integer.toString(keepAliveSeconds));
        logger.info("namePrefix: " + namePrefix);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置缓冲队列大小
        executor.setQueueCapacity(queueCapacity);
        // 设置线程的最大空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 设置线程名字的前缀
        executor.setThreadNamePrefix(namePrefix);
        // 设置拒绝策略：当线程池达到最大线程数时，如何处理新任务
        /*
         * AbortPolicy：默认策略。直接抛出 RejectedExecutionException
         * DiscardPolicy：直接丢弃掉被拒绝的任务，且不会抛出任何异常
         * DiscardOldestPolicy：丢弃掉队列中的队头元素（也就是最早在队列里的任务），然后重新执行 提交该任务 的操作
         * CallerRunsPolicy：由主线程自己来执行这个任务，该机制将减慢新任务的提交
         */
        // CALLER_RUNS：在添加到线程池失败时会由主线程自己来执行这个任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程池初始化
        executor.initialize();

        return executor;
    }
}