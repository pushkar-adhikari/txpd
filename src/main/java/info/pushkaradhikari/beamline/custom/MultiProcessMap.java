package info.pushkaradhikari.beamline.custom;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import beamline.models.responses.Response;
import info.pushkaradhikari.beamline.custom.dot.TxDotModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiProcessMap extends Response {

    private static final long serialVersionUID = 6248452599496121805L;
    
    private Map<String, Deque<String>> recentCaseIds;
    private Map<String, Map<String, ProcessMap>> perCaseProcessMaps;
    private Map<String, ProcessMap> updated;
    private Map<String, TxDotModel> dots;
    

    public MultiProcessMap() {
        recentCaseIds = new ConcurrentHashMap<>();
        perCaseProcessMaps = new ConcurrentHashMap<>();
        updated = new ConcurrentHashMap<>();
    }

    public void addProcessMap(String processName, String caseId, ProcessMap map) {
        perCaseProcessMaps.computeIfAbsent(processName, k -> new ConcurrentHashMap<>()).put(caseId, map);
        updated.put(caseId, map);
        updateCompositeProcessMap(processName, caseId);
        log.info("updated process {} caseId {}, current size: {}", processName, caseId, updated.size());
    }

    private void updateCompositeProcessMap(String processName, String newCaseId) {
        Deque<String> caseDeque = recentCaseIds.computeIfAbsent(processName, k -> new LinkedList<>());
        if (!caseDeque.contains(newCaseId)) {
            if (caseDeque.size() >= 20) {
                String oldestCaseId = caseDeque.removeFirst();
                log.info("removed caseId {} from case queue", oldestCaseId);
                Map<String, ProcessMap> caseProcessMaps = perCaseProcessMaps.get(processName);
                if (caseProcessMaps != null) {
                    caseProcessMaps.remove(oldestCaseId);
                    log.info("removed stale case {} from process maps", oldestCaseId);
                }
            }
            caseDeque.addLast(newCaseId);
        }
        ProcessMap compositeMap = createCompositeProcessMap(processName, caseDeque);
        updated.put(processName, compositeMap);
    }

    private ProcessMap createCompositeProcessMap(String processName, Deque<String> caseIds) {
        ProcessMap compositeMap = new ProcessMap(processName);
        List<ProcessMap> maps = caseIds.stream().map(caseId -> perCaseProcessMaps.get(processName).get(caseId))
                .collect(Collectors.toList());
        Map<String, Double> combinedActivityFrequencies = new HashMap<>();
        Map<Pair<String, String>, Double> combinedRelationFrequencies = new HashMap<>();

        double maxActivityFreq = 0d;
        double maxRelationFreq = 0d;

        for (ProcessMap map : maps) {
            for (String activity : map.getActivities()) {
                double freq = map.getActivityAbsoluteFrequency(activity);
                combinedActivityFrequencies.merge(activity, freq, Double::sum);
                maxActivityFreq = Math.max(maxActivityFreq, combinedActivityFrequencies.get(activity));
            }
            for (Pair<String, String> relation : map.getRelations()) {
                double freq = map.getRelationAbsoluteValue(relation);
                combinedRelationFrequencies.merge(relation, freq, Double::sum);
                maxRelationFreq = Math.max(maxRelationFreq, combinedRelationFrequencies.get(relation));
            }
            compositeMap.getStartingActivities().addAll(map.getStartingActivities());
            compositeMap.getEndActivities().addAll(map.getEndActivities());
        }
        
        final double maxActivityFreqResult = maxActivityFreq;
        final double maxRelationFreqResult = maxRelationFreq;
        combinedActivityFrequencies.forEach((activity, freq) -> {
            double relativeFreq = freq / maxActivityFreqResult;
            compositeMap.addActivity(activity, relativeFreq, freq);
        });
        combinedRelationFrequencies.forEach((relation, freq) -> {
            double relativeFreq = freq / maxRelationFreqResult;
            compositeMap.addRelation(relation.getLeft(), relation.getRight(), relativeFreq, freq);
        });
        return compositeMap;
    }

    public void generateDot() {
        dots = new HashMap<>();
        for (Map.Entry<String, ProcessMap> entry : updated.entrySet()) {
            String name;
            boolean isCaseSpecific = false;
            if (entry.getKey().contentEquals(entry.getValue().processName)) {
                name = entry.getKey();
            } else {
                name = entry.getValue().processName + "_" + entry.getKey();
                isCaseSpecific = true;
            }
            try {
                TxDotModel dot = entry.getValue().generateDot()
                        .setCaseSpecific(isCaseSpecific, entry.getKey());
                dots.put(name, dot);
                log.debug("digraph for process {}\n{}", name, dot.toString());
            } catch (Exception e) {
                log.error("Error creating dot file for process name: " + name, e);
            }
        }
        updated.clear();
    }
    
    public Map<String, TxDotModel> getDots() {
        return dots;
    }

    public ProcessMap getIndividualProcessMap(String processName, String caseId) {
        ProcessMap map = perCaseProcessMaps.getOrDefault(processName, Collections.emptyMap()).get(caseId);
        return null == map ? new ProcessMap(processName) : map;
    }

    @Override
    public String toString() {
        return perCaseProcessMaps.toString();
    }

}
