package info.pushkaradhikari.beamline.executor;

public interface AsyncDotProcessorMXBean {
    int getCountOfTotalModelsWrittenToInflux();
    int getCountOfTotalExportedModels();
}
