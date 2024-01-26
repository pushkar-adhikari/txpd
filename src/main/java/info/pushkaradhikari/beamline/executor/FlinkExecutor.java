package info.pushkaradhikari.beamline.executor;

import java.io.File;
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
		new File(txpdProperties.getResult().getLocation()).mkdirs();
		log.info("Starting FlinkExecutor...");
		
		final int parallelism = txpdProperties.getFlinkConfig().getParallelism();
		final int asyncSlots = txpdProperties.getFlinkConfig().getAsyncSlots();
		log.info("FlinkExecutor configuration: Parallelism: {}, Async Slots: {}", parallelism, asyncSlots);
		final Configuration configuration = new Configuration();
		configuration.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, parallelism + asyncSlots);
		configuration.setInteger(RestOptions.PORT, 8082);
		
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(parallelism, configuration);
		
		env.enableCheckpointing(30000);
		env.getCheckpointConfig().setMinPauseBetweenCheckpoints(60000);
		env.getCheckpointConfig().setCheckpointTimeout(60000);
		env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
		env.getCheckpointConfig().setTolerableCheckpointFailureNumber(1000);
		
		
		DataStream<MultiProcessMap> dotsStream = env.setParallelism(parallelism)
			.addSource(source)
			.keyBy(BEvent::getProcessName)
			.flatMap(new CustomDFDDiscoveryMiner());
		
		AsyncDataStream.orderedWait(
			dotsStream,
	        new AsyncDotProcessor(txpdProperties),
	        300000, TimeUnit.MILLISECONDS,
	        asyncSlots
	    );

		env.execute();
	}
}