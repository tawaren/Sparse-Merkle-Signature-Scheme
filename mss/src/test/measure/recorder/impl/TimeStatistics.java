package test.measure.recorder.impl;


import test.measure.recorder.IRecorderResult;

/**
 * Represents Results of a Time Measurement (as the Time Recorder creates it)
 * Excatly it measures Leaf and Hash Calculations
 */
public class TimeStatistics implements IRecorderResult{
    public final int hashValuesMax;            //the Maximal number of Hash values calculated in any rounds (inner nodes only)
    public final long roundWithHashMax;        //the round in which hashValuesMax occurs
    public final int hashValuesMin;            //the Minimal number of Hash values calculated in any rounds (inner nodes only)
    public final long roundWithHashMin;        //the round in which hashValuesMin occurs
    public final double averageHashValues;     //the average amount of hash values calculated in a rounds averaged over all rounds


    public final int leafValuesMax;            //the Maximal number of Leaf values calculated in any rounds
    public final long roundWithLeafMax;        //the round in which roundWithLeafMax occurs
    public final int leafValuesMin;            //the Minimal number of Leaf values calculated in any rounds
    public final long roundWithLeafMin;        //the round in which leafValuesMin occurs
    public final double averageLeafValues;     //the average amount of hash Leaf calculated in a rounds averaged over all rounds

    public final long rounds;                  //the total amount of rounds

    //Constructor (just filling the fields)
    public TimeStatistics(
            int hashValuesMax, long roundWithHashMax,
            int hashValuesMin, long roundWithHashMin,
            double averageHashValues,
            int leafValuesMax, long roundWithLeafMax,
            int leafValuesMin, long roundWithLeafMin,
            double averageLeafValues,
            long rounds
    ) {
        this.hashValuesMax = hashValuesMax;
        this.roundWithHashMax = roundWithHashMax;
        this.hashValuesMin = hashValuesMin;
        this.roundWithHashMin = roundWithHashMin;
        this.averageHashValues = averageHashValues;
        this.leafValuesMax = leafValuesMax;
        this.roundWithLeafMax = roundWithLeafMax;
        this.leafValuesMin = leafValuesMin;
        this.roundWithLeafMin = roundWithLeafMin;
        this.averageLeafValues = averageLeafValues;
        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return "TimeStatistics{" +
                "hashValuesMax=" + hashValuesMax +
                ", roundWithHashMax=" + roundWithHashMax +
                ", hashValuesMin=" + hashValuesMin +
                ", roundWithHashMin=" + roundWithHashMin +
                ", averageHashValues=" + averageHashValues +
                ", leafValuesMax=" + leafValuesMax +
                ", roundWithLeafMax=" + roundWithLeafMax +
                ", leafValuesMin=" + leafValuesMin +
                ", roundWithLeafMin=" + roundWithLeafMin +
                ", averageLeafValues=" + averageLeafValues +
                ", rounds=" + rounds +
                '}';
    }
}
