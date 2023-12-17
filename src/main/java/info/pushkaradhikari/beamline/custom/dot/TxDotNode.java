package info.pushkaradhikari.beamline.custom.dot;

import java.awt.Color;

import beamline.graphviz.DotNode;

public class TxDotNode extends DotNode {
	
	private String label;

	public TxDotNode(String label) {
		this(label, null, null, null);
	}

	public TxDotNode(String label, String secondLine, Double weight, TXColorPalette.Colors activityColor) {
		super(label, null);

		this.label = label;

		setOption("shape", "box");
		setOption("fixedsize", "true");
		setOption("height", "0.23");
		setOption("width", "1.2");
		setOption("style", "rounded,filled");
		setOption("fontname", "Arial");

		setSecondLine(secondLine);
		setColorWeight(weight, activityColor);
	}

	public void setSecondLine(String secondLine) {
		if (secondLine != null) {
			setLabel("<<font point-size=\"22\">" + label + "</font><br/><br/><font point-size=\"11\">" + secondLine + "</font>>");
		}
	}

	public void setColorWeight(Double weight, TXColorPalette.Colors activityColor) {
		if (weight == null) {
			setOption("fillcolor", "#FDEFD8"); // #FDEFD8:#E1D3BC
		} else {
			Color backgroundColor = TXColorPalette.getValue(activityColor, weight);
			Color fontColor = TXColorPalette.getFontColor(backgroundColor);
			setOption("fillcolor", TXColorPalette.colorToString(backgroundColor)/* + ":" + TXColorPalette.colorToString(backgroundColor.darker()) */);
			setOption("fontcolor", TXColorPalette.colorToString(fontColor));
			setOption("fixedsize", "false");
		}
	}

	public void setMovedIn() {
		setOption("fillcolor", "white");
	}

	public void setMovedOut() {
		setOption("fillcolor", "black");
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return getLabel().equals(object);
	}
}
