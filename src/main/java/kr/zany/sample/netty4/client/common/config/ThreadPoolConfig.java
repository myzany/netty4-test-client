package kr.zany.sample.netty4.client.common.config;

import kr.zany.sample.netty4.client.common.data.StaticVariables;
import kr.zany.sample.netty4.client.common.data.ThreadSettings;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;
import java.util.concurrent.Executor;

/**
 * <p><b>Class Description</b></p>
 * <p>Copyright ⓒ 2016 kt corp. All rights reserved.</p>
 *
 * @author Lee Sang-Hyun (zanylove@gmail.com)
 * @since 2016-01-04 21:08
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inject Beans
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final ThreadSettings threadSettings;


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Member Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constructor
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Inject
    public ThreadPoolConfig(ThreadSettings threadSettings) {
        this.threadSettings = threadSettings;
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Protected, Private Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Override Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    @Bean(name = {"asyncExecutor"}, destroyMethod = "shutdown")
    public Executor getAsyncExecutor() {

        int corePoolSize  = threadSettings.getCorePoolSize();
        int maxPoolSize   = threadSettings.getMaxPoolSize();
        int queueCapacity = threadSettings.getQueueCapacity();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        if (corePoolSize > -1) {
            executor.setCorePoolSize((corePoolSize > 0) ? corePoolSize : StaticVariables.AVAILABLE_PROCESSORS * 16);
        }

        if (maxPoolSize > -1) {
            executor.setMaxPoolSize((maxPoolSize > 0) ? maxPoolSize : StaticVariables.AVAILABLE_PROCESSORS * 64);
        }

        if (queueCapacity > -1) {
            executor.setQueueCapacity((queueCapacity > 0) ? queueCapacity : StaticVariables.AVAILABLE_PROCESSORS * 64);
        }

        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
