package test.measure.recorder.impl;


import test.measure.recorder.IRecorderResult;

/**
 * Represents Statistics from two different sources (where the sources could generate a split statistic itself if more then two real sources are needed)
 * @param <A> type of Result from source one
 * @param <B> type of Result from source two
 */
public class SplitStatistics<A extends IRecorderResult, B extends IRecorderResult> implements IRecorderResult{
   private final A resA;    //result from source one
   private final B resB;    //result from source one

   public SplitStatistics(A resA, B resB) {
        this.resA = resA;
        this.resB = resB;
   }

   @Override
   public String toString() {
        return "SplitStatistics{" +
                "resA=" + resA +
                ", resB=" + resB +
                '}';
   }
}
