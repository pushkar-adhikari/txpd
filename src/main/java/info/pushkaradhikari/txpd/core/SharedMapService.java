package info.pushkaradhikari.txpd.core;

import org.springframework.stereotype.Component;

import info.pushkaradhikari.beamline.source.KafkaSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SharedMapService {

    private final Map<String, KafkaSource> shareKafkaSources = new ConcurrentHashMap<>();

    public void put(String key, KafkaSource value) {
        shareKafkaSources.put(key, value);
    }

    public KafkaSource get(String key) {
        return shareKafkaSources.get(key);
    }

    public void remove(String key) {
        shareKafkaSources.remove(key);
    }

    public boolean containsKey(String key) {
        return shareKafkaSources.containsKey(key);
    }

}
