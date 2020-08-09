package test.measure.recorder;


import java.io.IOException;

/**
 * Intreface to alow recording Measurements in different sections
 * dynamic space
 * static space
 * hash computations
 * leaf computations
 * @param <T> is the result type
 */
public interface IRecorder<T extends IRecorderResult> {
    //these methods indicate if the Recorder can use some input
    boolean needsDynamicSpaceData();
    boolean needsStaticSpaceData();
    boolean needsHashData();
    boolean needsLeafData();
    boolean needsRoundData();

    //preparations
    void prepare() throws IOException;

    //this methods provide the actual Datas
    void hashCalculationDone();
    void leafCalculationDone(long leafIndex);
    void blockFinished(long round) throws IOException;
    void roundFinished(long round) throws IOException;
    void dynamicSpaceMeasurementDone(int space);

    //extract the results
    T genResult() throws IOException;


}
