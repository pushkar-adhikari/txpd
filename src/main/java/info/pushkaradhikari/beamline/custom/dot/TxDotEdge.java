package info.pushkaradhikari.beamline.custom.dot;

import beamline.graphviz.DotEdge;
import beamline.graphviz.DotNode;

public class TxDotEdge extends DotEdge {
	
	public TxDotEdge(DotNode source, DotNode target, String edgeText, Double weight) {
		super(source, target);

		setOption("decorate", "false");
		setOption("fontsize", "10");
		setOption("arrowsize", "0.5");
		setOption("fontname", "Arial");
		setOption("tailclip", "false");

		if (edgeText != null) {
			setLabel("    " + edgeText);
		}

		if (weight != null) {
			setOption("minlen", "2");
			setOption("color",
					TXColorPalette.colorToString(TXColorPalette.getValue(TXColorPalette.Colors.DARK_GRAY, weight)));
			if ((source instanceof TxDotStartNode) || (target instanceof TxDotEndNode)) {
				setOption("penwidth", "" + (1 + (5 * weight)));
			} else {
				setOption("penwidth", "" + (1 + (8 * weight)));
			}
		} else {
			if ((source instanceof TxDotStartNode) || (target instanceof TxDotEndNode)) {
				setOption("penwidth", "2");
			} else {
				setOption("penwidth", "3");
			}
		}

		if (source instanceof TxDotStartNode) {
			setOption("style", "dashed");
			setOption("color", "#ACB89C");
		}

		if (target instanceof TxDotStartNode) {
			setOption("style", "dashed");
			setOption("color", "#C2B0AB");
		}
	}

}
