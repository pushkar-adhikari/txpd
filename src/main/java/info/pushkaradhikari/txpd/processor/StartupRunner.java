package info.pushkaradhikari.txpd.processor;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import info.pushkaradhikari.txpd.processor.service.InitiatorService;
import info.pushkaradhikari.txpd.processor.service.MetricsProcessorService;

@Component
public class StartupRunner {

    private final InitiatorService initiatorService;
    private final MetricsProcessorService metricsProcessorService;

    public StartupRunner(
        InitiatorService initiatorService,
        MetricsProcessorService metricsProcessorService) {
        this.initiatorService = initiatorService;
        this.metricsProcessorService = metricsProcessorService;
    }

    @PostConstruct
    public void startServices() throws Exception {
        initiatorService.run();
//        metricsProcessorService.run();
    }
}