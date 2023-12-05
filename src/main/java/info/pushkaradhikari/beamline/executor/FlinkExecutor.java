package info.pushkaradhikari.beamline.executor;

import java.io.File;
import java.io.Serializable;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import beamline.events.BEvent;
import beamline.graphviz.Dot;
import beamline.miners.trivial.ProcessMap;
import beamline.sources.BeamlineAbstractSource;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import info.pushkaradhikari.beamline.overrides.TxpdDFDMiner;
import info.pushkaradhikari.beamline.overrides.TxpdProcessMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FlinkExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	public void run(String resultPath, BeamlineAbstractSource source) throws Exception {
		log.info("Starting FlinkExecutor...");
		// DirectlyFollowsDependencyDiscoveryMiner miner = new
		// DirectlyFollowsDependencyDiscoveryMiner();
		TxpdDFDMiner miner = new TxpdDFDMiner();
		miner.setMinDependency(0.3).setModelRefreshRate(1);

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// env.getConfig().disableClosureCleaner();
		env.setParallelism(1)
				.enableCheckpointing(10000)
				.addSource(source)
				.keyBy(BEvent::getProcessName)
				.flatMap(miner)
				.addSink(new SinkFunction<ProcessMap>() {
					public void invoke(ProcessMap value, Context context) throws Exception {
						log.info("Process map: {}", value);
						TxpdProcessMap txpdProcessMap = (TxpdProcessMap) value;
						Dot dot = txpdProcessMap.getMap().generateDot();
						log.info("Starting export for process: {}", txpdProcessMap.processName);
						Graphviz.useEngine(new GraphvizV8Engine());
						dot.exportToSvg(new File(resultPath + txpdProcessMap.processName + ".svg"));
						dot.exportToFile(new File(resultPath + txpdProcessMap.processName + ".dot"));
					};
				});

		// step 4: consumption of the results
		env.execute();
	}
}
