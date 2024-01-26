package info.pushkaradhikari.beamline.executor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
public class AsyncDotProcessor extends RichAsyncFunction<MultiProcessMap, Void> {

    private static final long serialVersionUID = -6445251845896376578L;
    private transient InfluxDB influxDB;
    private transient boolean writeResults;
    private transient boolean writeCompositeModel;
    private final TXPDProperties txpdProperties;

    public AsyncDotProcessor(TXPDProperties txpdProperties) {
        this.txpdProperties = txpdProperties;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        writeResults = txpdProperties.getResult().isEnabled();
        writeCompositeModel = txpdProperties.getModelConfig().getComposite().isEnabled();
        log.info("Writing results to file: {}", writeResults);
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
                String svg = Graphviz.fromString(dot.toString()).render(Format.SVG).toString();
                log.info(uuid + " SVG generation complete for process {}", name);
                exportToSVG(dot, name, uuid);
                writeSvgToInfluxDB(dot, svg);
            }).thenRun(() -> resultFuture.complete(Collections.emptyList()));
        }
    }

    private void writeSvgToInfluxDB(TxDotModel dot, String svg) {
        String measurement = "process_svg";
        long timestamp = 0;
        Map<String, Object> fields = new HashMap<>();
        if (dot.isCaseSpecific()) {
            measurement = "execution_svg";
            fields.put("packageLogId", dot.getCaseId());
            timestamp = toEpochMilli(dot.getModel().getPackageLogEnd());
        } else if (!writeCompositeModel) {
            log.info("Skipping writing composite model to influxdb.");
            return;
        }
        Map<String, String> tags = new HashMap<>();
        setTags(dot, tags);
        fields.put("svg", svg);
        fields.put("anchor", 1);

        Point point = Point.measurement(measurement)
                .time(timestamp, TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();

        influxDB.write(point);
    }

    private void setTags(TxDotModel dot, Map<String, String> tags) {
        tags.put("processName", dot.getModel().getProcessName());
        tags.put("packageName", dot.getModel().getPackageName());
        tags.put("projectName", dot.getModel().getProjectName());
        tags.put("projectId", dot.getModel().getProjectId());
        tags.put("packageId", dot.getModel().getPackageId());
    }

    private void exportToSVG(Dot dot, String name, String uuid) {
        if (writeResults) {
            try {
                log.info(uuid + " Writing svg to file for process: {}", name);
                dot.exportToSvg(new File(txpdProperties.getResult().getLocation() + name.replace(":", "_") + ".svg"));
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

    private long toEpochMilli(String dateTime) {
		String baseFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat baseDateFormat = new SimpleDateFormat(baseFormat);

		try {
			int dotIndex = dateTime.indexOf('.');
			if (dotIndex != -1 && dotIndex < dateTime.length() - 1) {
				String withoutFraction = dateTime.substring(0, dotIndex);
				String fraction = dateTime.substring(dotIndex + 1);
				Date parsedDate = baseDateFormat.parse(withoutFraction);
				long time = parsedDate.getTime();

				if (fraction.length() > 3) {
					fraction = fraction.substring(0, 3); // Truncate to milliseconds
				}
				while (fraction.length() < 3) {
					fraction += "0"; // Pad to three digits
				}
				long milliFraction = Integer.parseInt(fraction);
				return time + milliFraction;
			} else {
				// No fractional seconds
				return baseDateFormat.parse(dateTime).getTime();
			}
		} catch (ParseException e) {
			log.error("Error converting datetime string: " + dateTime, e);
		}
		return 0;
	}
}
