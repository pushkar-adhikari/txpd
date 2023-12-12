package info.pushkaradhikari.txpd.core.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class SharedService {

    private final Queue<JsonNode> messages = new ConcurrentLinkedQueue<>();
    private final Map<String, LocalDateTime> runningProcesses = new ConcurrentHashMap<>();

    public void putProcessStart(String key, LocalDateTime value) {
        runningProcesses.put(key, value);
    }

    public LocalDateTime getProcessStart(String key) {
        return runningProcesses.get(key);
    }

    public void removeProcess(String key) {
        runningProcesses.remove(key);
    }

    public boolean containsProcess(String key) {
        return runningProcesses.containsKey(key);
    }
    
    public void addMessage(JsonNode message) {
    	messages.offer(message);
    }

    public JsonNode getMessage() {
        return messages.poll();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public int size() {
        return messages.size();
    }
}
