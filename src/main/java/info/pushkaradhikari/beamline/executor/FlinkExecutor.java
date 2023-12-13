package info.pushkaradhikari.beamline.executor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import beamline.events.BEvent;
import beamline.sources.BeamlineAbstractSource;
import info.pushkaradhikari.beamline.custom.AsyncPostProcessor;
import info.pushkaradhikari.beamline.custom.CustomDFDDiscoveryMiner;
import info.pushkaradhikari.beamline.custom.MultiProcessMap;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class FlinkExecutor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public void run(TXPDProperties txpdProperties, BeamlineAbstractSource source) throws Exception {
		log.info("Starting FlinkExecutor...");
		
		CustomDFDDiscoveryMiner miner = new CustomDFDDiscoveryMiner();

		final int parallelism = 1;
		final Configuration configuration = new Configuration();
		configuration.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, 2);
		configuration.setInteger(RestOptions.PORT, 8082);
		
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(parallelism, configuration);
		
		env.enableCheckpointing(300000);
		env.getCheckpointConfig().setMinPauseBetweenCheckpoints(60000);
		env.getCheckpointConfig().setCheckpointTimeout(60000);
		env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
		env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);
		
		
		DataStream<MultiProcessMap> processMapStream = env.setParallelism(1)
			.addSource(source)
			.keyBy(BEvent::getProcessName)
			.flatMap(miner);
		
		AsyncDataStream.orderedWait(
	        processMapStream,
	        new AsyncPostProcessor(txpdProperties),
	        30000, TimeUnit.MILLISECONDS,
	        1
	    );

		env.execute();
	}
}