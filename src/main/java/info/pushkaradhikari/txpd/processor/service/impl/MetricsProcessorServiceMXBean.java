package info.pushkaradhikari.txpd.processor.service.impl;

import info.pushkaradhikari.txpd.processor.service.MetricsProcessorService;

public interface MetricsProcessorServiceMXBean extends MetricsProcessorService {
    int getCountOfTotalWrittenMetrics();

    int getCountOfProcessedMessages();

    int getCountOfWrittenPackageMetrics();

    int getCountOfWrittenStepMetrics();
}
