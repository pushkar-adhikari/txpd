package info.pushkaradhikari.txpd.processor.service.impl;

import org.influxdb.InfluxDB;

import info.pushkaradhikari.beamline.executor.FlinkExecutor;
import info.pushkaradhikari.beamline.source.KafkaSource;
import info.pushkaradhikari.txpd.core.business.annotation.TXPDService;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties;
import info.pushkaradhikari.txpd.processor.service.InitiatorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TXPDService("initiatorService")
public class InitiatorServiceImpl implements InitiatorService {

    private final TXPDProperties txpdProperties;
    private final KafkaSource kafkaSource;

    public InitiatorServiceImpl(
            TXPDProperties txpdProperties,
            KafkaSource kafkaSource,
            InfluxDB influxDB) {
        this.txpdProperties = txpdProperties;
        this.kafkaSource = kafkaSource;
    }

    public void run() throws Exception {
        log.info("Starting InitiatorService...");
        log.info("result location: {}", txpdProperties.getResult().getLocation());
        initEnvironment();
    }

    private void initEnvironment() {
        log.info("Initializing flink environment");
        new Thread(() -> {
            try {
                FlinkExecutor flinkExecutor = new FlinkExecutor();
                flinkExecutor.run(txpdProperties, kafkaSource);
            } catch (Exception e) {
                log.error("Error initializing environment", e);
            }
        }).start();
    }
}
