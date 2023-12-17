package info.pushkaradhikari.beamline.executor;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import beamline.graphviz.Dot;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import info.pushkaradhikari.beamline.custom.MultiProcessMap;
import info.pushkaradhikari.beamline.custom.dot.TxDotModel;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties;
import info.pushkaradhikari.txpd.core.business.config.TXPDProperties.InfluxConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncDotProcessor extends RichAsyncFunction<MultiProcessMap, Void>  {
	
	private static final long serialVersionUID = -6445251845896376578L;
	private transient InfluxDB influxDB;
	private transient boolean writeResults;
    private final TXPDProperties txpdProperties;
    
    public AsyncDotProcessor(TXPDProperties txpdProperties) {
        this.txpdProperties = txpdProperties;
    }
    
    @Override
    public void open(Configuration parameters) throws Exception {
    	writeResults = txpdProperties.getResult().isEnabled();
    	InfluxConfig config = txpdProperties.getInfluxConfig();
        influxDB = InfluxDBFactory.connect(config.getUrl(), config.getUsername(), config.getPassword())
                 .setDatabase(config.getDatabase());
    }
    
    @Override
    public void asyncInvoke(MultiProcessMap map, ResultFuture<Void> resultFuture) {
    	Map<String, TxDotModel> dots = map.getDots();
    	String uuid = UUID.randomUUID().toString();
    	log.info(uuid + " async invoked, rendering maps for {} processes", dots.keySet().size());
    	for (Map.Entry<String, TxDotModel> entry : dots.entrySet()) {
    		CompletableFuture.runAsync(() -> {
                String name = entry.getKey();
                TxDotModel dot = entry.getValue();
                log.info(uuid + " Starting SVG generation for process: {}", name);
                String svg = Graphviz.fromString(dot.toString()).render(Format.SVG).toString();
                log.info(uuid + " SVG generation complete for process {}", name);
                exportToSVG(dot, name, uuid);
                writeSvgToInfluxDB(dot, svg);
        	}).thenRun(() -> resultFuture.complete(Collections.emptyList()));
    	}
    }
    
    private void writeSvgToInfluxDB(TxDotModel dot, String svg) {
    	String measurement = dot.isCaseSpecific() ? "process_svg" : "execution_svg";
    	Map<String, String> tags = new HashMap<>();
        tags.put("processName", dot.getModel().getProcessName());
        if (dot.isCaseSpecific()) {
            tags.put("packageLogId", dot.getCaseId());
        }
        Map<String, Object> fields = new HashMap<>();
        fields.put("svg", svg);
        fields.put("anchor", 1);
        long timestamp = 0;
        
        Point point = Point.measurement(measurement)
            .time(timestamp, TimeUnit.MILLISECONDS)
            .tag(tags)
            .fields(fields)
            .build();

        influxDB.write(point);
    }
    
    private void exportToSVG(Dot dot, String name, String uuid) {
    	if (writeResults) {    		
    		try {                	
    			dot.exportToSvg(new File(txpdProperties.getResult().getLocation() + name + ".svg"));
    		} catch (Exception e) {
    			log.error(uuid + " Error writing svg to file!", e);
    		}
    	}
    }
    
    @Override
    public void close() throws Exception {
        if (influxDB != null) {
            influxDB.close();
        }
    }

}
