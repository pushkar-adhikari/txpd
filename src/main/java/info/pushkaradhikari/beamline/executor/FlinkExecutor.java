package info.pushkaradhikari.beamline.executor;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import beamline.events.BEvent;
import beamline.graphviz.Dot;
import beamline.sources.BeamlineAbstractSource;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import info.pushkaradhikari.beamline.custom.CustomDFDDiscoveryMiner;
import info.pushkaradhikari.beamline.custom.MultiProcessMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FlinkExecutor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final GraphvizV8Engine engine = new GraphvizV8Engine();

	public void run(String resultPath, BeamlineAbstractSource source) throws Exception {
		log.info("Starting FlinkExecutor...");
		
		CustomDFDDiscoveryMiner miner = new CustomDFDDiscoveryMiner();
		miner.setMinDependency(0.3).setModelRefreshRate(1);
		
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1)
				.enableCheckpointing(10000)
				.addSource(source)
				.keyBy(BEvent::getProcessName)
				.flatMap(miner)
				.addSink(new MultiProcessMapSink(resultPath));

		env.execute();
	}
	
	private final class MultiProcessMapSink implements SinkFunction<MultiProcessMap> {
		private static final long serialVersionUID = -1655184285808454692L;
		private final String resultPath;

		private MultiProcessMapSink(String resultPath) {
			this.resultPath = resultPath;
		}

		public void invoke(MultiProcessMap value, Context context) throws Exception {
			log.info("Process map: {}", value);
			Map<String,Dot> dots = value.generateDot();
			for (Map.Entry<String, Dot> entry : dots.entrySet()) {
				String processName = entry.getKey();
				Dot dot = entry.getValue();
				log.info("Starting export for process: {}", processName);
				Graphviz.useEngine(engine);
				dot.exportToSvg(new File(resultPath + processName + ".svg"));
				dot.exportToFile(new File(resultPath + processName + ".dot"));
			}
		}
	}
}
