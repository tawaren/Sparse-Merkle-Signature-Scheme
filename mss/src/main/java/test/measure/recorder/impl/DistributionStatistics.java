package test.measure.recorder.impl;


import test.measure.recorder.IRecorderResult;

import java.util.Map;

/**
 * Statistics for measure Bounded leakage (indicator for Side Channel Attack resistance)
 * How many times is each leaf calculated
 */
public class DistributionStatistics implements IRecorderResult{
    Map<Long,Integer> data ;        //key is leaf index, value is amount of calculations of that leaf

    public DistributionStatistics(Map<Long, Integer> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DistributionStatistics{" +
                "data=" + data +
                '}';
    }
}
