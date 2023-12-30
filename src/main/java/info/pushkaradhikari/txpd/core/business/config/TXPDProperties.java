package info.pushkaradhikari.txpd.core.business.config;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "txpd")
public class TXPDProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private Result result;

    @Valid
    private KafkaConfig kafkaConfig;

    @Valid
    private InfluxConfig influxConfig;

    @Getter
    @Setter
    public static class Result implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String location;
        
        private boolean enabled;
        
        
    }

    @Getter
    @Setter
    public static class KafkaConfig implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String bootstrapServers;

        @NotBlank
        private String topic;

        @NotBlank
        private String autoOffsetReset;

        @NotBlank
        private String enableAutoCommit;
        
        @Valid
        private Groups groups;
        
        @NotNull
        private int maxPollRecords;
        
        @Getter
        @Setter
        public static class Groups implements Serializable {
            
            private static final long serialVersionUID = 1L;
            
            @NotBlank
            private String miner;

            @NotBlank
            private String processor;
            
        }
    }

    @Getter
    @Setter
    public static class InfluxConfig implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String url;

        @NotBlank
        private String username;

        @NotBlank
        private String password;

        @NotBlank
        private String database;
    }

}
