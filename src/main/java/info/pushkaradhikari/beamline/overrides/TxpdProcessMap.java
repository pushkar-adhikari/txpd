package info.pushkaradhikari.beamline.overrides;

import beamline.miners.trivial.ProcessMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TxpdProcessMap extends ProcessMap {
	public String processName;
    public ProcessMap map;

    public TxpdProcessMap(ProcessMap map, String processName) {
        this.map = map;
        this.processName = processName;
    }
}
