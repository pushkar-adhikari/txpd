package info.pushkaradhikari.beamline.source;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

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
					log.info(uuid + " - BEvent collected for process: " + event.getProcessName());
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
		// props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        return new KafkaConsumer<>(props);
	}

	private BEvent extractEvent(ConsumerRecord<String, JsonNode> record) {
        try {
			log.info(uuid + " - Received message from kafka");
			JsonNode jsonNode = record.value();
			String projectName = jsonNode.get("ProjectName").asText();
			String packageName = jsonNode.get("PackageName").asText();
			String packageLogId = jsonNode.get("PackageLogId").asText();
			String packageLogDetailName = jsonNode.get("PackageLogDetailName").asText();
			//String packageLogDetailName = jsonNode.get("DetailStepAction").asText();
			String processName = projectName + "_" + packageName;
			BEvent event = new BEvent(processName, packageLogId, packageLogDetailName);
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

	// @Override
	// public void run(SourceContext<BEvent> ctx) throws Exception {
	// 	while (isRunning()) {
	// 		synchronized (ctx.getCheckpointLock()) {
	// 			while (isRunning() && buffer.isEmpty()) {
	// 				log.info("Waiting for BEvent...");
	// 				ctx.getCheckpointLock().wait(100); // use wait instead of sleep
	// 			}
	// 			if (isRunning() && !buffer.isEmpty()) {
	// 				BEvent e = buffer.poll();
    //                 log.info("BEvent received: " + e);
	// 				ctx.collect(e);
	// 			}
	// 		}
	// 	}
	// }

	// @KafkaListener(topics = "log_replay", groupId = "txpd")
	// public void listen(JsonNode jsonNode) {
	// 	try {
	// 		log.info("Received message from kafka");
	// 		String projectName = jsonNode.get("ProjectName").asText();
	// 		String packageName = jsonNode.get("PackageName").asText();
	// 		String packageLogId = jsonNode.get("PackageLogId").asText();
	// 		String packageLogDetailName = jsonNode.get("PackageLogDetailName").asText();
	// 		String processName = projectName + "_" + packageName;
	// 		BEvent event = new BEvent(processName, packageLogId, packageLogDetailName);
	// 		synchronized (buffer) {
	// 			buffer.add(event);
	// 			buffer.notifyAll();
	// 			log.info("Notified source buffer with BEvent");
	// 		}
	// 	} catch (Exception e) {
	// 		log.error("Error JSON...", e);
	// 	}
	// }
}