package info.pushkaradhikari.txpd.processor.service.impl;

import static info.pushkaradhikari.txpd.core.util.TXPDUtil.toEpochMilli;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import info.pushkaradhikari.txpd.core.business.annotation.TXPDService;
import info.pushkaradhikari.txpd.core.mapper.JacksonObjectMapper;
import info.pushkaradhikari.txpd.core.util.SharedService;
import info.pushkaradhikari.txpd.processor.dto.EventDTO;
import info.pushkaradhikari.txpd.processor.service.MetricsProcessorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TXPDService("metricsProcessorService")
public class MetricsProcessorServiceImpl implements MetricsProcessorService {

    private final SharedService sharedService;
    private final InfluxDB influxDB;

    public MetricsProcessorServiceImpl(
            SharedService sharedService,
            InfluxDB influxDB) {
        this.sharedService = sharedService;
        this.influxDB = influxDB;
    }

    @Async
    public void run() {
        log.info("Starting MetricsProcessorService...");
        while (true) {
            if (!sharedService.isEmpty()) {
                JsonNode jsonNode = sharedService.getMessage();
                processMessage(jsonNode);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error sleeping thread", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processMessage(JsonNode jsonNode) {
        log.trace("Processing message: {}", jsonNode);
        JacksonObjectMapper mapper = new JacksonObjectMapper();
        try {
            EventDTO eventDTO = mapper.treeToValue(jsonNode, EventDTO.class);
            processEvent(eventDTO);
        } catch (JsonProcessingException e) {
            log.error("Error converting jsonNode to EventDTO", e);
        } catch (Exception e) {
            log.error("Error processing jsonNode: " + jsonNode, e);
        }
    }

    private void processEvent(EventDTO eventDTO) {
        Map<String, String> tags = new HashMap<>();
        tags.put("packageId", eventDTO.getPackageId());
        tags.put("packageName", eventDTO.getPackageName());
        tags.put("projectId", eventDTO.getProjectId());
        tags.put("projectName", eventDTO.getProjectName());
        tags.put("packageLogId", eventDTO.getPackageLogId());
        processPackage(eventDTO, tags);
        processStep(eventDTO, tags);
    }

    private void processStep(EventDTO eventDTO, Map<String, String> tags) {
        if (eventDTO.getPackageLogDetailEnd() != null) {
            String measurement = "package_step";
            tags.put("stepName", eventDTO.getPackageLogDetailName());

            Map<String, Object> fields = new HashMap<>();
            long stepStart = toEpochMilli(eventDTO.getPackageLogDetailStart());
            long stepEnd = toEpochMilli(eventDTO.getPackageLogDetailEnd());
            fields.put("stepStart", stepStart);
            fields.put("stepEnd", stepEnd);
            fields.put("stepDuration", stepEnd - stepStart);
            fields.put("stepStatus", eventDTO.getPackageLogDetailEndStatus());
            
            writeMetric(measurement, tags, fields, stepEnd);
        }
    }

    private void processPackage(EventDTO eventDTO, Map<String, String> tags) {
        
        if (!sharedService.containsProcess(eventDTO.getPackageLogId())) {
            sharedService.putProcessStart(eventDTO.getPackageLogId(), eventDTO.getPackageLogStart());
        }
        if (eventDTO.getPackageLogEnd() != null) {
            String measurement = "package";

            Map<String, Object> fields = new HashMap<>();
            long processStart = toEpochMilli(sharedService.getProcessStart(eventDTO.getPackageLogId()));
            long processEnd = toEpochMilli(eventDTO.getPackageLogEnd());
            fields.put("packageLogStart", processStart);
            fields.put("packageLogEnd", processEnd);
            fields.put("packageDuration", processEnd - processStart);
            fields.put("packageStatus", eventDTO.getPackageLogEndStatus());
            sharedService.removeProcess(eventDTO.getPackageLogId());

            writeMetric(measurement, tags, fields, processEnd);
        }
    }

    private void writeMetric(String measurement, Map<String, String> tags, Map<String, Object> fields, long time) {
        Point.Builder builder = Point.measurement(measurement);
        if (tags != null) {
            builder.tag(tags);
        }
        if (fields != null) {
            builder.fields(fields);
        }
        builder.time(time, TimeUnit.MILLISECONDS);
        Point point = builder.build();
        influxDB.write(point);
    }
}
