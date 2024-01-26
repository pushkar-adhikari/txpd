package info.pushkaradhikari.beamline.source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import beamline.events.BEvent;
import beamline.sources.BeamlineAbstractSource;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties.KafkaConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaSource extends BeamlineAbstractSource implements CheckpointedFunction  {

    private static final long serialVersionUID = 608025607423103621L;

    private long offset = 0L; // offset of the last record consumed
    private final String uuid;
    private final TXPDProperties txpdProperties;

    public volatile boolean running = true;
    private transient ListState<Long> offsetState;
    private transient KafkaConsumer<String, JsonNode> consumer;

    public KafkaSource(TXPDProperties txpdProperties) {
        this.txpdProperties = txpdProperties;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public void run(SourceContext<BEvent> ctx) throws Exception {
        KafkaConfig kafkaConfig = txpdProperties.getKafkaConfig();
        consumer = createKafkaConsumer(kafkaConfig);
        log.info(uuid + " - Subscribing to topic: " + kafkaConfig.getTopic());
        consumer.subscribe(Collections.singletonList(kafkaConfig.getTopic()));
        try {
            while (isRunning()) {
                ConsumerRecords<String, JsonNode> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                log.info(uuid + " - Received " + records.count() + " records");
                for (ConsumerRecord<String, JsonNode> record : records) {
                    BEvent event = extractEvent(record);
                    log.info(uuid + " - Event collected for process: " + event.getProcessName());
                    if (isRunning()) {
                        synchronized (ctx.getCheckpointLock()) {
                            ctx.collect(event);
                            offset = record.offset(); // Update the current offset
                        }
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

    private KafkaConsumer<String, JsonNode> createKafkaConsumer(KafkaConfig kafkaConfig) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getGroups().getMiner());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfig.getAutoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConfig.getMaxPollRecords());
        return new KafkaConsumer<>(props);
    }

    private BEvent extractEvent(ConsumerRecord<String, JsonNode> record) {
        try {
            JsonNode jsonNode = record.value();
            String projectName = jsonNode.get("ProjectName").asText();
            String projectId = jsonNode.get("ProjectId").asText();
            String packageName = jsonNode.get("PackageName").asText();
            String packageId = jsonNode.get("PackageId").asText();
            String packageLogId = jsonNode.get("PackageLogId").asText();
            String packageLogEnd = jsonNode.get("PackageLogEnd").asText();
            String packageLogDetailName = jsonNode.get("PackageLogDetailName").asText();
            List<Pair<String, String>> eventAttributes = new ArrayList<>();
            eventAttributes.add(Pair.of("projectId", projectId));
            eventAttributes.add(Pair.of("projectName", projectName));
            eventAttributes.add(Pair.of("packageId", packageId));
            eventAttributes.add(Pair.of("packageName", packageName));
            eventAttributes.add(Pair.of("packageLogEnd", packageLogEnd));
            String processName = projectName + ":" + packageName;
            BEvent event = new BEvent(processName, packageLogId, packageLogDetailName, null, eventAttributes);
            return event;
        } catch (Exception e) {
            log.error(uuid + " - Error JSON...", e);
        }
        return null; // should not happen
    }

    @Override
    public void cancel() {
        running = false;
        if (consumer != null) {
            consumer.close();
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        offsetState.clear();
        offsetState.add(offset);
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<Long> descriptor = new ListStateDescriptor<>("kafkaOffsetState", Long.class);

        offsetState = context.getOperatorStateStore().getListState(descriptor);

        if (context.isRestored()) {
            for (Long state : offsetState.get()) {
                offset = state;
            }
        }
    }
}