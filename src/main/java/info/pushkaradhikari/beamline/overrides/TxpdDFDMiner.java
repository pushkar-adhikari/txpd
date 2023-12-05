package info.pushkaradhikari.beamline.overrides;

import beamline.events.BEvent;
import beamline.miners.trivial.DirectlyFollowsDependencyDiscoveryMiner;
import beamline.miners.trivial.ProcessMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TxpdDFDMiner extends DirectlyFollowsDependencyDiscoveryMiner {
    public TxpdDFDMiner() {
        super();
    }

    public TxpdDFDMiner setMinDependency(double minDependency) {
        super.setMinDependency(minDependency);
        return this;
    }

    public TxpdDFDMiner setModelRefreshRate(int modelRefreshRate) {
        super.setModelRefreshRate(modelRefreshRate);
        return this;
    }

    @Override
	public ProcessMap ingest(BEvent event) {
        ProcessMap map = super.ingest(event);
        if (map != null) {
            log.info("Map created for event: {}", event.getProcessName());
            return new TxpdProcessMap(map, event.getProcessName());
        } else {
            log.error("Map is null for event: {}", event.getProcessName());
            return null;
        }
    }
}
