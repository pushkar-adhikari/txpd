package info.pushkaradhikari.beamline.custom;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.events.BEvent;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class CustomDFDDiscoveryMiner extends StreamMiningAlgorithm<MultiProcessMap> {

	private static final long serialVersionUID = -6688122589937218288L;
	private MultiProcessMap multiProcessMap = new MultiProcessMap();
	private Map<String, Miner> miners = new ConcurrentHashMap<>();

	@Override
	public MultiProcessMap ingest(BEvent event) {
		String processName = event.getProcessName();
		if (processName.contentEquals("unnamed-xes-process")) {
			// Fix for unnamed processes while using XESLogSource
			String projectName = (String) event.getEventAttributes().get("project:name");
			String packageName = (String) event.getEventAttributes().get("package:name");
			if (projectName != null && packageName != null) {
				processName = projectName + "_" + packageName;
			}
		}
		String caseID = event.getTraceName();
		Miner miner = miners.computeIfAbsent(caseID, k -> {
			return new Miner();
		});
		ProcessMap individualMap = multiProcessMap.getIndividualProcessMap(processName, caseID);
		individualMap = miner.ingest(event, individualMap);
		if (individualMap != null) {
            multiProcessMap.addProcessMap(processName, caseID, individualMap);
        }
		multiProcessMap.generateDot();
		return multiProcessMap;
	}

	private class Miner implements Serializable {

		private static final long serialVersionUID = -1788972505878844643L;
		private Map<String, String> lastCaseIdForActivity = new LinkedHashMap<>();
		private Map<String, String> latestActivityInCase = new LinkedHashMap<>();
		private Map<Pair<String, String>, Double> relations = new LinkedHashMap<>();
		private Map<String, Double> activities = new LinkedHashMap<>();
		private Map<String, Double> startingActivities = new LinkedHashMap<>();
		private Double maxActivityFreq = 1d;
		private Double maxRelationsFreq = 1d;
		private double minDependency = 0.01;
		private int modelRefreshRate = 1;

		public Miner() {
		}

		public ProcessMap ingest(BEvent event, ProcessMap oldMap) {
			String caseID = event.getTraceName();
			String activityName = event.getEventName();
			Double activityFreq = 1d;
			if (activities.containsKey(activityName)) {
				activityFreq += activities.get(activityName);
				maxActivityFreq = Math.max(maxActivityFreq, activityFreq);
			}
			activities.put(activityName, activityFreq);
			if (latestActivityInCase.containsKey(caseID)) {
				String lastActivity = latestActivityInCase.get(caseID);
				Pair<String, String> relation = new ImmutablePair<>(lastActivity, activityName);
				if (caseID.equals(lastCaseIdForActivity.get(lastActivity))) {
					Double relationFreq = 1d;
					if (relations.containsKey(relation)) {
						relationFreq += relations.get(relation);
						maxRelationsFreq = Math.max(maxRelationsFreq, relationFreq);
					}
					relations.put(relation, relationFreq);
				}
			} else {
				Double count = 1d;
				if (startingActivities.containsKey(activityName)) {
					count += startingActivities.get(activityName);
				}
				startingActivities.put(activityName, count);
			}
			latestActivityInCase.put(caseID, activityName);
			lastCaseIdForActivity.put(activityName, caseID);

			if (getProcessedEvents() % modelRefreshRate == 0) {
				return mine(minDependency, oldMap);
			}
			return null;
		}

		public ProcessMap mine(double threshold, ProcessMap process) {
			for (Entry<String, Double> entry : activities.entrySet()) { 
				String activity = entry.getKey();
		        Double frequency = entry.getValue();
		        process.addActivity(activity, frequency / maxActivityFreq, frequency);
			}
			if (activities.size() == 1 && relations.isEmpty()) {
				String soleActivity = activities.keySet().iterator().next();
				process.addStartingActivity(soleActivity);
				process.addEndActivity(soleActivity);
			} else {
				if (relations.size() == 1) {
					process.clearEndActivities();
				}
				for (Entry<Pair<String, String>, Double> entry : relations.entrySet()) {
					double dependency = entry.getValue() / maxRelationsFreq;
					if (dependency >= threshold) {
						process.addRelation(entry.getKey().getLeft(), entry.getKey().getRight(), dependency,
								entry.getValue());
					}
				}
				Set<String> toRemove = new LinkedHashSet<>();
				Set<String> selfLoopsToRemove = new LinkedHashSet<>();
				for (String activity : activities.keySet()) {
					if (process.isStartActivity(activity) && process.isEndActivity(activity)) {
						toRemove.add(activity);
					}
					if (process.isIsolatedNode(activity)) {
						selfLoopsToRemove.add(activity);
					}
				}
				for (String activity : toRemove) {
					process.removeActivity(activity);
				}
				for (Entry<String, Double> entry : startingActivities.entrySet()) {
					Double freq = entry.getValue();
					double dependency = freq / maxRelationsFreq;
					if (dependency >= threshold) {
						process.addStartingActivity(entry.getKey());
					}
				}
			}
			return process;
		}
	}
}
