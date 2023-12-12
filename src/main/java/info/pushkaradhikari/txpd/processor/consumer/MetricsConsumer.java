package info.pushkaradhikari.txpd.processor.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import info.pushkaradhikari.txpd.core.util.SharedService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetricsConsumer {
    
    private final SharedService sharedService;
    
    public MetricsConsumer(
            SharedService sharedService) {
        this.sharedService = sharedService;
    }

    @KafkaListener(topics = "log_replay", groupId = "txpd-processor")
    public void listen(JsonNode jsonNode) {
        log.trace("Received message from kafka");
        sharedService.addMessage(jsonNode);
    }
}
