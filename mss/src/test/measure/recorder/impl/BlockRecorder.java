package test.measure.recorder.impl;


import test.measure.recorder.IRecorder;
import test.measure.recorder.IRecorderResult;

import java.io.IOException;

/**
 * Recorder which change the behavior of another recorder by lenghtening a round (finish round is only forwarded all blockSize rounds)
 * This leads to a reduced amount of Data in the log file (but the data is less accurate)
 * @param <T> result type of the wrapped recorder
 */
public class BlockRecorder<T extends IRecorderResult> implements IRecorder<T> {

    private final IRecorder<T> rec;     //the other recorder whichs behavior is changed
    private final int blockSize;        //the block size (all blockSize rounds a round is finished on rec)
    private int count;                  //counter to track passed rounds

    //Constructor
    public BlockRecorder(int blockSize, IRecorder<T> rec) {
        this.rec = rec;
        this.blockSize = blockSize;

    }

    @Override
    public boolean needsDynamicSpaceData() {
        return rec.needsDynamicSpaceData();
    }

    @Override
    public boolean needsStaticSpaceData() {
        return rec.needsStaticSpaceData();
    }

    @Override
    public boolean needsHashData() {
        return rec.needsHashData();
    }

    @Override
    public boolean needsLeafData() {
        return rec.needsLeafData();
    }

    @Override
    public boolean needsRoundData() {
        return rec.needsRoundData();
    }

    @Override
    public void hashCalculationDone() {
        rec.hashCalculationDone();
    }

    @Override
    public void leafCalculationDone(long leafIndex) {
        rec.leafCalculationDone(leafIndex);
    }

    @Override
    public void roundFinished(long round) throws IOException {
        //increase counter and check if it matches blocksize
        rec.roundFinished(round);
        if(++count == blockSize){
            count = 0;                  //reset counter
            rec.blockFinished(round);   //forward round
        }
    }

    @Override
    public void blockFinished(long round) throws IOException { }

    @Override
    public void dynamicSpaceMeasurementDone(int space) {
        rec.dynamicSpaceMeasurementDone(space);
    }

    @Override
    public void prepare() throws IOException {
        count = 0;
        rec.prepare();
    }

    @Override
    public T genResult() throws IOException {
        return rec.genResult();
    }
}
