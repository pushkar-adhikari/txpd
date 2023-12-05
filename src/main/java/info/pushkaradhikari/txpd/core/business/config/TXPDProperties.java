package info.pushkaradhikari.txpd.core.business.config;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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

    @Getter
    @Setter
    public static class Result implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotBlank
        private String location;
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
        private String groupId;

        @NotBlank
        private String autoOffsetReset;

        @NotBlank
        private String enableAutoCommit;
    }

}
