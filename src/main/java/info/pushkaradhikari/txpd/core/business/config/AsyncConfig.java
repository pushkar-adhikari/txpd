package info.pushkaradhikari.txpd.core.business.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        return Executors.newFixedThreadPool(5);
    }
}
