package info.pushkaradhikari.beamline.custom.dot;

import beamline.graphviz.DotNode;

public class TxDotEndNode extends DotNode {

	public TxDotEndNode() {
		super("", null);

		setOption("shape", "circle");
		setOption("style", "filled");
		setOption("fillcolor", "#D8BBB9"); // #D8BBB9:#BC9F9D
		setOption("gradientangle", "270");
		setOption("color", "#614847");
		setOption("height", "0.13");
		setOption("width", "0.13");
	}

	@Override
	public String toString() {
		return "{ rank = \"sink\"; " + super.toString() + "}";
	}
}
