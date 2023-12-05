package info.pushkaradhikari.txpd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import beamline.events.BEvent;
import beamline.graphviz.Dot;
import beamline.miners.trivial.DirectlyFollowsDependencyDiscoveryMiner;
import beamline.miners.trivial.ProcessMap;
import beamline.sources.XesLogSource;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
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
		DirectlyFollowsDependencyDiscoveryMiner miner = new DirectlyFollowsDependencyDiscoveryMiner();
		miner.setMinDependency(0.3);

		// step 3: construction of the dataflow from the environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(8)
				.addSource(source)
				.keyBy(BEvent::getProcessName)
				.flatMap(miner)
				.addSink(new SinkFunction<ProcessMap>() {
					public void invoke(ProcessMap value, Context context) throws Exception {
						Dot dot = value.generateDot();
						Graphviz.useEngine(new GraphvizV8Engine());
						dot.exportToSvg(new File(out_folder + fileName.replace(".xes", "_XES_SOURCE.svg")));
						// dot.exportToFile(new File(out_folder + fileName.replace(".xes", ".dot")));
					};
				});

		// step 4: consumption of the results
		env.execute();
	}

}