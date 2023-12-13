package info.pushkaradhikari.beamline.custom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import beamline.graphviz.Dot;
import beamline.miners.trivial.graph.ColorPalette;
import beamline.models.responses.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiProcessMap extends Response {

    private static final long serialVersionUID = 6248452599496121805L;
    
    private Map<String, ProcessMap> processMaps;
    private Map<String, LocalDateTime> insertionTimes;
    private Map<String, Boolean> updateChecks;
    

    public MultiProcessMap() {
        processMaps = new ConcurrentHashMap<>();
        insertionTimes = new ConcurrentHashMap<>();
        updateChecks = new ConcurrentHashMap<>();
    }

    public Map<String, Dot> generateDot() {
        Map<String, Dot> dots = new HashMap<>();
        for (Map.Entry<String, ProcessMap> entry : processMaps.entrySet()) {
        	String processName = entry.getKey();
        	if (updateChecks.get(processName)) {
        		try {
        			dots.put(processName, new DotModel(entry.getValue(), ColorPalette.Colors.BLUE));
        			updateChecks.put(processName, false);
        		} catch (Exception e) {
        			log.error("Error creating dot file for process name: :" + processName, e);
        		}
        	}
        }
        return dots;
    }

    public void addProcessMap(String processName, ProcessMap map) {
        removeStaleProcessMaps(Duration.ofHours(3));
        if (!checkProcessMap(processName)) {
            insertionTimes.put(processName, LocalDateTime.now());
        }
        processMaps.put(processName, map);
        updateChecks.put(processName, true);
    }

    public ProcessMap getProcessMap(String processName) {
        return processMaps.get(processName);
    }

    public Map<String, ProcessMap> getProcessMaps() {
        return processMaps;
    }

    public boolean checkProcessMap(String processName) {
        return processMaps.containsKey(processName);
    }

    void removeStaleProcessMaps(Duration threshold) {
        LocalDateTime now = LocalDateTime.now();
        List<String> staleProcessNames = insertionTimes.entrySet().stream()
            .filter(entry -> Duration.between(entry.getValue(), now).compareTo(threshold) > 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        log.info("Removing stale process maps: {}", staleProcessNames);
        for (String processName : staleProcessNames) {
            processMaps.remove(processName);
            insertionTimes.remove(processName);
        }
    }

    @Override
    public String toString() {
        return processMaps.toString();
    }

}
