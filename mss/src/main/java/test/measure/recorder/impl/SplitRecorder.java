package test.measure.recorder.impl;


import test.measure.recorder.IRecorder;
import test.measure.recorder.IRecorderResult;

import java.io.IOException;

/**
 * Forward the Measurements to two Recorders and at the end collects the two Results (the recorders could be splitrecorders itself)
 * @param <A> type of Result from recorder one
 * @param <B> type of Result from recorder two
 */
public class SplitRecorder<A extends IRecorderResult,B extends IRecorderResult> implements IRecorder<SplitStatistics<A,B>> {

    private IRecorder<A> recA;      //recorder one
    private IRecorder<B> recB;      //recorder two

    public SplitRecorder(IRecorder<A> recA, IRecorder<B> recB) {
        this.recA = recA;
        this.recB = recB;
    }

    @Override
    public boolean needsDynamicSpaceData() {
        return recA.needsDynamicSpaceData() || recB.needsDynamicSpaceData();    //does one of the two recorders need the data?
    }

    @Override
    public boolean needsStaticSpaceData() {
        return recA.needsStaticSpaceData() || recB.needsStaticSpaceData();      //does one of the two recorders need the data?
    }

    @Override
    public boolean needsHashData() {
        return recA.needsHashData() || recB.needsHashData();                    //does one of the two recorders need the data?
    }

    @Override
    public boolean needsLeafData() {
        return recA.needsLeafData() || recB.needsLeafData();                    //does one of the two recorders need the data?
    }

    @Override
    public boolean needsRoundData() {
        return recA.needsRoundData() || recB.needsRoundData();                  //does one of the two recorders need the data?
    }

    @Override
    public void hashCalculationDone() {
        if(recA.needsHashData())recA.hashCalculationDone();            //if the data is needed by recorder one forward it
        if(recB.needsHashData())recB.hashCalculationDone();            //if the data is needed by recorder two forward it
    }

    @Override
    public void leafCalculationDone(long leafIndex) {
        if(recA.needsLeafData())recA.leafCalculationDone(leafIndex);            //if the data is needed by recorder one forward it
        if(recB.needsLeafData())recB.leafCalculationDone(leafIndex);            //if the data is needed by recorder two forward it
    }

    @Override
    public void blockFinished(long round) throws IOException {
        if(recA.needsRoundData())recA.blockFinished(round);            //if the data is needed by recorder one forward it
        if(recB.needsRoundData())recB.blockFinished(round);            //if the data is needed by recorder two forward it
    }

    @Override
    public void roundFinished(long round) throws IOException {
        if(recA.needsRoundData())recA.roundFinished(round);            //if the data is needed by recorder one forward it
        if(recB.needsRoundData())recB.roundFinished(round);            //if the data is needed by recorder two forward it
    }

    @Override
    public void dynamicSpaceMeasurementDone(int space) {
        if(recA.needsDynamicSpaceData())recA.dynamicSpaceMeasurementDone(space);            //if the data is needed by recorder one forward it
        if(recB.needsDynamicSpaceData())recB.dynamicSpaceMeasurementDone(space);            //if the data is needed by recorder two forward it
    }

    @Override
    public void prepare() throws IOException {
        //Prepare both Recorders
        recA.prepare();
        recB.prepare();
    }

    @Override
    public SplitStatistics<A, B> genResult() throws IOException {
        //Collect both results
        return new SplitStatistics<>(recA.genResult(),recB.genResult());
    }
}
