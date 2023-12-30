package info.pushkaradhikari.txpd.core.business.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TXPDProperties.class)
public class TXPDAutoConfig {
    
    @Bean
    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
    public TXPDProperties txpdProperties() {
        return new TXPDProperties();
    }
}
