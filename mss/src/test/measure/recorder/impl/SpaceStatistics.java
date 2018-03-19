package test.measure.recorder.impl;


import test.measure.recorder.IRecorderResult;

/**
 * Represents Results of a Space Measurement (as the Dyn Space Recorder creates it)
 */
public class SpaceStatistics implements IRecorderResult{
    public final int spaceMax;            //the maximal space needed in a round over all rounds
    public final int spaceMin;            //the minimal space needed in a round over all rounds
    public final double spaceAvgMax;      //the average over the max space needed in each round over all rounds
    public final double spaceAvgMin;      //the average over the min space needed in each round over all rounds


    public final long roundMax;           //round where the spaceMax occurs
    public final long roundMin;           //round where the spaceMin occurs
    public final long rounds;             //the total amount of rounds

    //Constructor (just filling the fields)
    public SpaceStatistics(int spaceMax, long roundMax, int spaceMin, long roundMin, double spaceAvgMax, double spaceAvgMin, long rounds) {
        this.spaceMax = spaceMax;
        this.spaceMin = spaceMin;
        this.spaceAvgMax = spaceAvgMax;
        this.spaceAvgMin = spaceAvgMin;

        this.roundMax = roundMax;
        this.roundMin = roundMin;
        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return "SpaceStatistics{" +
                "spaceMax=" + spaceMax +
                ", spaceMin=" + spaceMin +
                ", spaceAvgMax=" + spaceAvgMax +
                ", spaceAvgMin=" + spaceAvgMin +
                ", roundMax=" + roundMax +
                ", roundMin=" + roundMin +
                ", rounds=" + rounds +
                '}';
    }
}
