package info.pushkaradhikari.beamline.custom;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import beamline.graphviz.Dot;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties.InfluxConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncPostProcessor extends RichAsyncFunction<MultiProcessMap, Void>  {
	
	private static final long serialVersionUID = -6445251845896376578L;
	private transient InfluxDB influxDB;
    private final TXPDProperties txpdProperties;
    
    public AsyncPostProcessor(TXPDProperties txpdProperties) {
        this.txpdProperties = txpdProperties;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
    	InfluxConfig config = txpdProperties.getInfluxConfig();
        influxDB = InfluxDBFactory.connect(config.getUrl(), config.getUsername(), config.getPassword())
                 .setDatabase(config.getDatabase());
    }
    
    @Override
    public void asyncInvoke(MultiProcessMap input, ResultFuture<Void> resultFuture) {
        CompletableFuture.runAsync(() -> {
        	log.info("async invoked, rendering maps...");
            Map<String, Dot> dots = input.generateDot();
            for (Map.Entry<String, Dot> entry : dots.entrySet()) {
                String processName = entry.getKey();
                Dot dot = entry.getValue();
                log.info("Starting SVG generation for process: {}", processName);
                Graphviz.useEngine(new GraphvizV8Engine());
                String svg = Graphviz.fromString(dot.toString()).render(Format.SVG).toString();
                log.info("SVG generation complete for process {}", processName);
                try {                	
                	dot.exportToSvg(new File(txpdProperties.getResult().getLocation() + processName + ".svg"));
                } catch (Exception e) {
					log.error("Error writing svg to file!", e);
				}
                writeSvgToInfluxDB(processName, svg);
            }
        }).thenRun(() -> resultFuture.complete(Collections.emptyList()));
    }
    
    private void writeSvgToInfluxDB(String processName, String svg) {
        // Logic to write SVG to InfluxDB
    }
    
    @Override
    public void close() throws Exception {
        if (influxDB != null) {
            influxDB.close();
        }
    }

}
