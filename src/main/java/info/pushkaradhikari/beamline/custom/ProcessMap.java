package info.pushkaradhikari.beamline.custom;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.graphviz.Dot;
import beamline.models.responses.GraphvizResponse;
import lombok.extern.slf4j.Slf4j;
import beamline.miners.trivial.graph.ColorPalette;

@Slf4j
public class ProcessMap extends GraphvizResponse {

	private static final long serialVersionUID = 6248452599496125805L;
	private Map<String, Pair<Double, Double>> activities;
	private Map<Pair<String, String>, Pair<Double, Double>> relations;
	private Set<String> startingActivities;
	private Set<String> endingActivities;
	final String processName;

	@Override
	public Dot generateDot() {
		return new DotModel(this, ColorPalette.Colors.BLUE);
	}

	public ProcessMap(String processName) {
		this.activities = new LinkedHashMap<>();
		this.relations = new LinkedHashMap<>();
		this.startingActivities = new LinkedHashSet<>();
		this.endingActivities = new LinkedHashSet<>();
		this.processName = processName;
	}

	public void addActivity(String activityName, Double relativeFrequency, Double absoluteFrequency) {
		log.debug(processName + " adding activity " + activityName + " with freq: " + relativeFrequency + ", " + absoluteFrequency);
		this.activities.put(activityName, Pair.of(relativeFrequency, absoluteFrequency));
	}

	public void removeActivity(String activityName) {
		log.debug(processName + " removing activity " + activityName);
		this.activities.remove(activityName);
	}

	public void addRelation(String activitySource, String activityTarget, Double relativeFrequency, Double absoluteFrequency) {
		log.debug(processName + " adding relation " + activitySource + ", " + activityTarget + " with freq: " + relativeFrequency + ", " + absoluteFrequency);
		relations.put(Pair.of(activitySource, activityTarget), Pair.of(relativeFrequency, absoluteFrequency));
	}

	public void removeRelation(String activitySource, String activityTarget) {
		log.debug(processName + " removing relation " + activitySource + ", " + activityTarget);
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
		log.debug(processName + " getActivityRelativeFrequency " + activity + ": " + result);
		return result;
	}
	
	public Double getActivityAbsoluteFrequency(String activity) {
		Double result = this.activities.getOrDefault(activity, Pair.of(1d,1d)).getRight();
		log.debug(processName + " getActivityAbsoluteFrequency " + activity + ": " + result);
		return result;
	}

	public Double getRelationRelativeValue(Pair<String, String> relation) {
		Double result = this.relations.get(relation).getLeft();
		log.debug(processName + " getRelationRelativeValue " + relation + ": " + result);
		return result;
	}
	
	public Double getRelationAbsoluteValue(Pair<String, String> relation) {
		Double result = this.relations.get(relation).getRight();
		log.debug(processName + " getRelationAbsoluteValue " + relation + ": " + result);
		return result;
	}

	public Set<String> getIncomingActivities(String candidate) {
		Set<String> result = new LinkedHashSet<>();
		for (Pair<String, String> relation : getRelations()) {
			if (relation.getRight().equals(candidate)) {
				result.add(relation.getLeft());
			}
		}
		log.debug(processName + " IncomingActivities for " + candidate + ":" + result);
		return result;
	}

	public Set<String> getOutgoingActivities(String candidate) {
		Set<String> result = new LinkedHashSet<>();
		for (Pair<String, String> relation : getRelations()) {
			if (relation.getLeft().equals(candidate)) {
				result.add(relation.getRight());
			}
		}
		log.debug(processName + " OutgoingActivities for " + candidate + ":" + result);
		return result;
	}
	
	public void addStartingActivity(String activity) {
		log.debug(processName + " addStartingActivity " + activity);
		startingActivities.add(activity);
	}
	
	public void clearStartingActivities() {
		log.debug(processName + " clearStartingActivities ");
		startingActivities.clear();
	}
	
	public void addEndActivity(String activity) {
		log.debug(processName + " addEndActivity " + activity);
		endingActivities.add(activity);
	}
	
	public void clearEndActivities() {
		log.debug(processName + " clearEndActivities ");
		endingActivities.clear();
	}

	public boolean isStartActivity(String candidate) {
		Boolean result = getIncomingActivities(candidate).isEmpty() || startingActivities.contains(candidate);
		log.debug(processName + " isStartActivity " + candidate + ": " + result);
		return result;
	}

	public boolean isEndActivity(String candidate) {
		Boolean result = getOutgoingActivities(candidate).isEmpty() || endingActivities.contains(candidate);
		log.debug(processName + " isEndActivity " + candidate + ": " + result);
		return result;
	}

	public boolean isIsolatedNode(String candidate) {
		Boolean result = getOutgoingActivities(candidate).equals(getIncomingActivities(candidate));
		log.debug(processName + " isIsolatedNode " + candidate + ": " + result);
		return result;
	}
}