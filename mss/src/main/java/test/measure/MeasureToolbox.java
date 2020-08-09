package test.measure;

import test.measure.recorder.IRecorder;
import test.measure.recorder.IRecorderResult;

import java.io.IOException;

/**
 * Toolbox to assits measuring
 * (is not thread safe)
 */
public class MeasureToolbox {

    //basic infos to publish which stuff is needed by the active recorder
    public static boolean needsDynamicSpaceData = false;
    public static boolean needsStaticSpaceData = false;
    public static boolean needsHashData = false;
    public static boolean needsLeafData = false;
    public static boolean needsRoundData = false;

    //the active recorder
    private static IRecorder recorder;

    public static <T extends IRecorderResult>  T measureWith(Runnable body, IRecorder<T> recorderForBlock) throws IOException {
       recorder = recorderForBlock;

        //publish the right values
       needsDynamicSpaceData  = recorderForBlock.needsDynamicSpaceData();
       needsStaticSpaceData  = recorderForBlock.needsStaticSpaceData();
       needsHashData  = recorderForBlock.needsHashData();
       needsLeafData  = recorderForBlock.needsLeafData();
       needsRoundData  = recorderForBlock.needsRoundData();

        //Excecute the measurement code
       recorderForBlock.prepare();
       body.run();
       T res = recorderForBlock.genResult();

        //fallback to default (no recorder active)
       needsDynamicSpaceData  = false;
       needsStaticSpaceData  = false;
       needsHashData  = false;
       needsLeafData  = false;
       needsRoundData  = false;
       recorder = null;
       return res;        //return the result
    }

    //forwards measurement if its active (hash)
    public static void emitHashCalculation(){
        if(needsHashData && recorder != null) recorder.hashCalculationDone();
    }

    //forwards measurement if its active (leaf)
    public static void emitLeaveCalculation(long leafIndex){
        if(needsLeafData && recorder != null) recorder.leafCalculationDone(leafIndex);
    }

    //forwards rounds finishing
    public static void finishRound(long round){
         if(needsRoundData && recorder != null) {
             try {
                 recorder.roundFinished(round);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
    }

    //forwards measurement if its active (space)
    public static void emitDynamicSpaceMeasurement(int space){
        if(needsDynamicSpaceData &&recorder != null) recorder.dynamicSpaceMeasurementDone(space);
    }

}
