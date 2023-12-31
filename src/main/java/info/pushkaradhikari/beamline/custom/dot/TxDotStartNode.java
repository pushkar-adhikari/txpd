package info.pushkaradhikari.beamline.custom.dot;

import beamline.graphviz.DotNode;

public class TxDotStartNode extends DotNode {
	public TxDotStartNode() {
		super("", null);

		setOption("shape", "circle");
		setOption("style", "filled");
		setOption("fillcolor", "#CED6BD"); // #CED6BD:#B3BBA2
		setOption("gradientangle", "270");
		setOption("color", "#595F45");
		setOption("height", "0.13");
		setOption("width", "0.13");
	}

	@Override
	public String toString() {
		return "{ rank = \"source\"; " + super.toString() + " }";
	}
}
