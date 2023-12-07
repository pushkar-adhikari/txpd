package info.pushkaradhikari.txpd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import beamline.events.BEvent;
import beamline.graphviz.Dot;
import beamline.miners.trivial.ProcessMap;
import beamline.sources.XesLogSource;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import info.pushkaradhikari.beamline.custom.CustomDFDDiscoveryMiner;
import info.pushkaradhikari.beamline.custom.MultiProcessMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestApplication {

	private static String in_folder = "/Users/pushkaradhikari/studies/thesis/txpd/xes-bkp/";
	private static String out_folder = "/Users/pushkaradhikari/studies/thesis/txpd/output/";

	public static void main(String[] args) throws Exception {
		log.info("Starting...");
		start();
	}

	private static void start() throws Exception {
		run("DSA_ADK_course.xes");
		// Files.list(Path.of(in_folder)).filter(p -> p.toString().contentEquals("DSA_ADK_course.xes")).forEach(p -> {
		// 	try {
		// 		run(p.getFileName().toString());
		// 	} catch (Exception e) {
		// 		e.printStackTrace();
		// 	}
		// });
	}

	private static void run(String fileName) throws Exception {
		// step 1: configuration of the event source (in this case a static file, for
		// reproducibility)
		XesLogSource source = new XesLogSource(in_folder + fileName);

		// step 2: configuration of the algorithm
		CustomDFDDiscoveryMiner miner = new CustomDFDDiscoveryMiner();
		miner.setMinDependency(0.3).setModelRefreshRate(1);

		// step 3: construction of the dataflow from the environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(8)
				.addSource(source)
				.keyBy(BEvent::getProcessName)
				.flatMap(miner)
				.addSink(new SinkFunction<MultiProcessMap>() {
					public void invoke(MultiProcessMap value, Context context) throws Exception {
						Map<String,Dot> dots = value.generateDot();
						for (Map.Entry<String, Dot> entry : dots.entrySet()) {
							String processName = entry.getKey();
							Dot dot = entry.getValue();
							Graphviz.useEngine(new GraphvizV8Engine());
							dot.exportToSvg(new File(out_folder + processName + "_XES_SOURCE.svg"));
							dot.exportToFile(new File(out_folder + processName + "_XES_SOURCE.dot"));
						}
					};
				});

		// step 4: consumption of the results
		env.execute();
	}

}