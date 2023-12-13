package info.pushkaradhikari.beamline.custom;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import beamline.graphviz.Dot;
import beamline.graphviz.DotNode;
import beamline.miners.trivial.graph.ColorPalette;
import beamline.miners.trivial.graph.PMDotEdge;
import beamline.miners.trivial.graph.PMDotEndNode;
import beamline.miners.trivial.graph.PMDotNode;
import beamline.miners.trivial.graph.PMDotStartNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DotModel extends Dot {

	private static final long serialVersionUID = -5125589037558963924L;
	private ProcessMap model;
	private ColorPalette.Colors activityColor;

	public DotModel(ProcessMap model, ColorPalette.Colors activityColor) {
		this.model = model;
		this.activityColor = activityColor;

		realize();
	}

	private void realize() {
		setOption("ranksep", ".1");
		setOption("fontsize", "9");
		setOption("remincross", "true");
		setOption("margin", "0.0,0.0");
		setOption("outputorder", "edgesfirst");

		Map<String, DotNode> activityToNode = new LinkedHashMap<>();
		Map<String, String> nodeToActivity = new LinkedHashMap<>();

		Set<DotNode> startNodes = new LinkedHashSet<>();
		Set<DotNode> endNodes = new LinkedHashSet<>();

		// add all activities
		for (String activity : model.getActivities()) {
			DotNode node = addNodeIfNeeded(activity, activityToNode, nodeToActivity);
			if (node instanceof PMDotNode) {
				((PMDotNode) node).setColorWeight(model.getActivityRelativeFrequency(activity), activityColor);
			}
			if (model.isStartActivity(activity)) {
				startNodes.add(node);
			}
			if (model.isEndActivity(activity)) {
				endNodes.add(node);
			}
		}
		log.debug(model.processName + " realize() nodes" + super.getNodes());
		log.debug(model.processName + " realize() start nodes" + startNodes);
		log.debug(model.processName + " realize() end nodes" + endNodes);

		// add all relations
		for (Pair<String, String> relation : model.getRelations()) {
			String sourceActivity = relation.getLeft();
			String targetActivity = relation.getRight();

			// adding source nodes
			DotNode sourceNode = addNodeIfNeeded(sourceActivity, activityToNode, nodeToActivity);
			// adding target nodes
			DotNode targetNode = addNodeIfNeeded(targetActivity, activityToNode, nodeToActivity);

			// adding relations
			addRelation(sourceNode, targetNode, model.getRelationRelativeValue(relation),
					model.getRelationAbsoluteValue(relation));
		}
		log.debug(model.processName + " realize() edges" + super.getEdges());

		// add relations from start and end
		if (!startNodes.isEmpty()) {
			PMDotStartNode start = new PMDotStartNode();
			addNode(start);
			for (DotNode n : startNodes) {
				addRelation(start, n, null, null);
			}
		}
		if (!endNodes.isEmpty()) {
			PMDotEndNode end = new PMDotEndNode();
			addNode(end);
			for (DotNode n : endNodes) {
				addRelation(n, end, null, null);
			}
		}
	}

	private void addRelation(DotNode sourceNode, DotNode targetNode, Double relativeFrequency,
			Double absoluteFrequency) {
		String freqLabel = "";
		if (relativeFrequency != null && absoluteFrequency != null) {
			freqLabel = String.format("%.2g ", relativeFrequency) + "(" + absoluteFrequency.intValue() + ")";
		}
		addEdge(new PMDotEdge(sourceNode, targetNode, freqLabel, relativeFrequency));
	}

	private DotNode addNodeIfNeeded(String activity, Map<String, DotNode> activityToNode,
			Map<String, String> nodeToActivity) {
		DotNode existingNode = activityToNode.get(activity);
		if (existingNode == null) {
			PMDotNode newNode = new PMDotNode(activity.toString());
			newNode.setColorWeight(model.getActivityRelativeFrequency(activity), activityColor);
			newNode.setSecondLine(String.format("%.2g%n", model.getActivityRelativeFrequency(activity)) + " ("
					+ model.getActivityAbsoluteFrequency(activity).intValue() + ")");
			addNode(newNode);
			activityToNode.put(activity, newNode);
			nodeToActivity.put(newNode.getId(), activity);
			return newNode;
		} else {
			return existingNode;
		}
	}
}