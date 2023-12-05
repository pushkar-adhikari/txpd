package info.pushkaradhikari.txpd.processor;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import info.pushkaradhikari.txpd.processor.service.InitiatorService;

@Component
public class StartupRunner {

    private final InitiatorService initiatorService;

    public StartupRunner(InitiatorService initiatorService) {
        this.initiatorService = initiatorService;
    }

    @PostConstruct
    public void startKafkaListener() throws Exception {
        initiatorService.run();
    }
}