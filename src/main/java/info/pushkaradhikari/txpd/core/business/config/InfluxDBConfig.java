package info.pushkaradhikari.txpd.core.business.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {
    
    private final TXPDProperties txpdProperties;

    public InfluxDBConfig(TXPDProperties txpdProperties) {
        this.txpdProperties = txpdProperties;
    }

    @Bean
    public InfluxDB influxDB() {
        return InfluxDBFactory.connect(
            txpdProperties.getInfluxConfig().getUrl(),
            txpdProperties.getInfluxConfig().getUsername(),
            txpdProperties.getInfluxConfig().getPassword()
        ).setDatabase(txpdProperties.getInfluxConfig().getDatabase());
    }
}
