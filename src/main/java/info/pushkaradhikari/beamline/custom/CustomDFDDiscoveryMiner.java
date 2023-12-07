package info.pushkaradhikari.beamline.custom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import beamline.events.BEvent;
import beamline.miners.trivial.ProcessMap;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class CustomDFDDiscoveryMiner extends StreamMiningAlgorithm<MultiProcessMap> {

	private static final long serialVersionUID = -6688122589937218288L;
	private MultiProcessMap multiProcessMap = new MultiProcessMap();
	private Map<String, Miner> miners = new ConcurrentHashMap<>();
	private double minDependency = 0.8;
	private int modelRefreshRate = 3;

	public CustomDFDDiscoveryMiner setMinDependency(double minDependency) {
		this.minDependency = minDependency;
		return this;
	}

	public CustomDFDDiscoveryMiner setModelRefreshRate(int modelRefreshRate) {
		this.modelRefreshRate = modelRefreshRate;
		return this;
	}

	@Override
	public MultiProcessMap ingest(BEvent event) {
		String processName = event.getProcessName();
		miners.computeIfAbsent(processName, k -> {
			Miner miner = new Miner();
			miner.setMinDependency(minDependency).setModelRefreshRate(modelRefreshRate);
			return miner;
		});
		Miner miner = miners.get(processName);
		ProcessMap map;
		if (multiProcessMap.checkProcessMap(processName)) {
			map = multiProcessMap.getProcessMap(processName);
			map = miner.ingest(event, map);
		} else {
			map = miner.ingest(event, new ProcessMap());
		}
		if (map != null) {
			multiProcessMap.addProcessMap(processName, map);
			return multiProcessMap;
		}
		return null;
	}

	private class Miner implements Serializable {

		private static final long serialVersionUID = -1788972505878844643L;
		private Map<String, String> lastCaseIdForActivity = new HashMap<>();
		private Map<String, String> latestActivityInCase = new HashMap<>();
		private Map<Pair<String, String>, Double> relations = new HashMap<>();
		private Map<String, Double> activities = new HashMap<>();
		private Map<String, Double> startingActivities = new HashMap<>();
		private Double maxActivityFreq = 1d;
		private Double maxRelationsFreq = 1d;
		private double minDependency = 0.8;
		private int modelRefreshRate = 10;

		public Miner() {
		}

		public Miner setMinDependency(double minDependency) {
			this.minDependency = minDependency;
			return this;
		}

		public Miner setModelRefreshRate(int modelRefreshRate) {
			this.modelRefreshRate = modelRefreshRate;
			return this;
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
				process.addActivity(entry.getKey(), entry.getValue() / maxActivityFreq, entry.getValue());
			}
			if (activities.size() == 1 && relations.isEmpty()) {
				String soleActivity = activities.keySet().iterator().next();
				process.addStartingActivity(soleActivity);
				process.addEndActivity(soleActivity);
			} else {
				for (Entry<Pair<String, String>, Double> entry : relations.entrySet()) {
					double dependency = entry.getValue() / maxRelationsFreq;
					if (dependency >= threshold) {
						process.addRelation(entry.getKey().getLeft(), entry.getKey().getRight(), dependency,
								entry.getValue());
					}
				}
				Set<String> toRemove = new HashSet<>();
				Set<String> selfLoopsToRemove = new HashSet<>();
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
