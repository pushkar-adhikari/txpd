package info.pushkaradhikari.beamline.custom;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.models.responses.GraphvizResponse;
import info.pushkaradhikari.beamline.custom.dot.TXColorPalette.Colors;
import info.pushkaradhikari.beamline.custom.dot.TxDotModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessMap extends GraphvizResponse {

    private static final long serialVersionUID = 6248452599496125805L;
    private Map<String, Pair<Double, Double>> activities;
    private Map<Pair<String, String>, Pair<Double, Double>> relations;
    private Set<String> startingActivities;
    private Set<String> endingActivities;
    final String processName;
    @Getter
    @Setter
    String projectId;
    @Getter
    final String projectName;
    @Getter
    @Setter
    String packageId;
    @Getter
    final String packageName;
    @Getter
    @Setter
    String packageLogEnd;

    @Override
    public TxDotModel generateDot() {
        return new TxDotModel(this, Colors.BLUE);
    }

    public ProcessMap(String processName) {
        this.activities = new LinkedHashMap<>();
        this.relations = new LinkedHashMap<>();
        this.startingActivities = new LinkedHashSet<>();
        this.endingActivities = new LinkedHashSet<>();
        this.processName = processName;
        this.projectName = processName.split(":")[0];
        this.packageName = processName.split(":")[1];
    }

    public void setMetaInfo(Map<String, Serializable> eventAttributes) {
        this.projectId = eventAttributes.get("projectId").toString();
        this.packageId = eventAttributes.get("packageId").toString();
        this.packageLogEnd = eventAttributes.get("packageLogEnd").toString();
    }

    public void addActivity(String activityName, Double relativeFrequency, Double absoluteFrequency) {
        log.trace(processName + " adding activity " + activityName + " with freq: " + relativeFrequency + ", " + absoluteFrequency);
        this.activities.put(activityName, Pair.of(relativeFrequency, absoluteFrequency));
    }

    public void removeActivity(String activityName) {
        log.trace(processName + " removing activity " + activityName);
        this.activities.remove(activityName);
    }

    public void addRelation(String activitySource, String activityTarget, Double relativeFrequency, Double absoluteFrequency) {
        log.trace(processName + " adding relation " + activitySource + ", " + activityTarget + " with freq: " + relativeFrequency + ", " + absoluteFrequency);
        relations.put(Pair.of(activitySource, activityTarget), Pair.of(relativeFrequency, absoluteFrequency));
    }

    public void removeRelation(String activitySource, String activityTarget) {
        log.trace(processName + " removing relation " + activitySource + ", " + activityTarget);
        relations.remove(new ImmutablePair<String, String>(activitySource, activityTarget));
    }

    public Set<String> getActivities() {
        return activities.keySet();
    }

    public Set<Pair<String, String>> getRelations() {
        return relations.keySet();
    }
    
    public Double getActivityRelativeFrequency(String activity) {
        Double result = this.activities.getOrDefault(activity, Pair.of(1d,1d)).getLeft();
        log.trace(processName + " getActivityRelativeFrequency " + activity + ": " + result);
        return result;
    }
    
    public Double getActivityAbsoluteFrequency(String activity) {
        Double result = this.activities.getOrDefault(activity, Pair.of(1d,1d)).getRight();
        log.trace(processName + " getActivityAbsoluteFrequency " + activity + ": " + result);
        return result;
    }

    public Double getRelationRelativeValue(Pair<String, String> relation) {
        Double result = this.relations.get(relation).getLeft();
        log.trace(processName + " getRelationRelativeValue " + relation + ": " + result);
        return result;
    }
    
    public Double getRelationAbsoluteValue(Pair<String, String> relation) {
        Double result = this.relations.get(relation).getRight();
        log.trace(processName + " getRelationAbsoluteValue " + relation + ": " + result);
        return result;
    }

    public Set<String> getIncomingActivities(String candidate) {
        Set<String> result = new LinkedHashSet<>();
        for (Pair<String, String> relation : getRelations()) {
            if (relation.getRight().equals(candidate)) {
                result.add(relation.getLeft());
            }
        }
        log.trace(processName + " IncomingActivities for " + candidate + ":" + result);
        return result;
    }

    public Set<String> getOutgoingActivities(String candidate) {
        Set<String> result = new LinkedHashSet<>();
        for (Pair<String, String> relation : getRelations()) {
            if (relation.getLeft().equals(candidate)) {
                result.add(relation.getRight());
            }
        }
        log.trace(processName + " OutgoingActivities for " + candidate + ":" + result);
        return result;
    }
    
    public void addStartingActivity(String activity) {
        log.trace(processName + " addStartingActivity " + activity);
        startingActivities.add(activity);
    }
    
    public void clearStartingActivities() {
        log.trace(processName + " clearStartingActivities ");
        startingActivities.clear();
    }
    
    public Set<String> getStartingActivities() {
        return startingActivities;
    }
    
    public void addEndActivity(String activity) {
        log.trace(processName + " addEndActivity " + activity);
        endingActivities.add(activity);
    }
    
    public void clearEndActivities() {
        log.trace(processName + " clearEndActivities ");
        endingActivities.clear();
    }
    
    public Set<String> getEndActivities() {
        return endingActivities;
    }

    public boolean isStartActivity(String candidate) {
        Boolean result = getIncomingActivities(candidate).isEmpty() || startingActivities.contains(candidate);
        log.trace(processName + " isStartActivity " + candidate + ": " + result);
        return result;
    }

    public boolean isEndActivity(String candidate) {
        Boolean result = getOutgoingActivities(candidate).isEmpty() || endingActivities.contains(candidate);
        log.trace(processName + " isEndActivity " + candidate + ": " + result);
        return result;
    }

    public boolean isIsolatedNode(String candidate) {
        Boolean result = getOutgoingActivities(candidate).equals(getIncomingActivities(candidate));
        log.trace(processName + " isIsolatedNode " + candidate + ": " + result);
        return result;
    }
    
    public String getProcessName() {
        return processName;
    }
}