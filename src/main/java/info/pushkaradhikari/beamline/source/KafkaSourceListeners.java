package info.pushkaradhikari.beamline.source;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import beamline.events.BEvent;
import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
public class KafkaSourceListeners {

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
	// 		KafkaSource kafkaSource = KafkaSource.getInstance();
	// 		synchronized (kafkaSource.buffer) {
	// 			kafkaSource.buffer.add(event);
	// 			kafkaSource.buffer.notifyAll();
	// 			log.info("Notified source buffer with BEvent");
	// 		}
	// 	} catch (Exception e) {
	// 		log.error("Error JSON...", e);
	// 	}
	// }
}
