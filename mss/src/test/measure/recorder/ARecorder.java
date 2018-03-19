package test.measure.recorder;


import java.io.IOException;

/**
 * Abstract class for IRecorder with default implementations
 * @param <T> is same as in IRecorder
 */
public abstract class ARecorder<T extends IRecorderResult> implements IRecorder<T> {
    @Override
    public boolean needsDynamicSpaceData() {
        return false;
    }

    @Override
    public boolean needsStaticSpaceData() {
        return false;
    }

    @Override
    public boolean needsHashData() {
        return false;
    }

    @Override
    public boolean needsLeafData() {
        return false;
    }

    @Override
    public boolean needsRoundData() {
        return false;
    }

    @Override
    public void hashCalculationDone() {}

    @Override
    public void leafCalculationDone(long leafIndex) {}

    @Override
    public void blockFinished(long round) throws IOException {}

    @Override
    public void roundFinished(long round) throws IOException {}

    @Override
    public void dynamicSpaceMeasurementDone(int space) {}
}
