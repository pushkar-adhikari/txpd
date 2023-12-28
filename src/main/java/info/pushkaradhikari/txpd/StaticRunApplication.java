package info.pushkaradhikari.txpd;

import java.io.File;
import java.util.Map;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import beamline.events.BEvent;
import beamline.sources.XesLogSource;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import info.pushkaradhikari.beamline.custom.CustomDFDDiscoveryMiner;
import info.pushkaradhikari.beamline.custom.MultiProcessMap;
import info.pushkaradhikari.beamline.custom.dot.TxDotModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticRunApplication {

    private static String in_folder = "./";
    private static String out_folder = "./output/";

    public static void main(String[] args) throws Exception {
        String filename = args[0];
        in_folder = args[1];
        out_folder = args[2];
        new File(out_folder).mkdirs();
        log.info("Starting...");
        run(filename);
    }

    private static void run(String fileName) throws Exception {
        Graphviz.useEngine(new GraphvizV8Engine());
        // step 1: configuration of the event source (in this case a static file, for
        // reproducibility)
        XesLogSource source = new XesLogSource(in_folder + fileName);

        // step 2: configuration of the algorithm
        CustomDFDDiscoveryMiner miner = new CustomDFDDiscoveryMiner();

        // step 3: construction of the dataflow from the environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(8)
                .addSource(source)
                .keyBy(BEvent::getProcessName)
                .flatMap(miner)
                .addSink(new SinkFunction<MultiProcessMap>() {
                    public void invoke(MultiProcessMap value, Context context) throws Exception {
                        Map<String,TxDotModel> dots = value.getDots();
                        for (Map.Entry<String, TxDotModel> entry : dots.entrySet()) {
                            String processName = entry.getKey();
                            TxDotModel dot = entry.getValue();
                            dot.exportToSvg(new File(out_folder + processName + "_XES_SOURCE.svg"));
                            dot.exportToFile(new File(out_folder + processName + "_XES_SOURCE.dot"));
                        }
                    };
                });

        // step 4: consumption of the results
        env.execute();
    }
}