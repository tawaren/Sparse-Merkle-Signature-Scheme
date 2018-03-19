package test.measure.recorder.impl;


import test.measure.logger.ILogger;
import test.measure.recorder.ARecorder;

import java.io.IOException;

/**
 * Measures Leaf and Hash values calculated and creates a TimeStatistics
 * (if a logger is provided the round datas are pushed to the logger in addition)
 */
public class TimeRecorder extends ARecorder<TimeStatistics> {
    //the backing logger (null if none)
    private final ILogger logger;

    //Constructor with Logger
    public TimeRecorder(ILogger logger) {
        this.logger = logger;
    }

    //Constructor without Logger
    public TimeRecorder() {
        this.logger = null;
    }

    @Override
    public boolean needsHashData() {
        return true;
    }

    @Override
    public boolean needsLeafData() {
        return true;
    }

    @Override
    public boolean needsRoundData() {
        return true;
    }

    private int hashValuesMaxBlock;  //the Maximal number of Hash values calculated in a round during this Block (inner nodes only)
    private int hashValuesMax;      //the Maximal number of Hash values calculated in a round until now (inner nodes only)
    private long roundWithHashMax;  //the round in which hashValuesMax occurs
    private int hashValuesMin;      //the Minimal number of Hash values calculated in a round until now (inner nodes only)
    private long roundWithHashMin;  //the round in which hashValuesMin occurs
    private int roundHashValues;    //the hash values calculated in the current round
    private int blockHashValues;    //the hash values calculated in the current block

    private long maxHashSum;        //all hash values calculated until now

    private int leafValuesMaxBlock;  //the Maximal number of Leaf values calculated in a round during this Block (inner nodes only)
    private int leafValuesMax;      //the Maximal number of Leaf values calculated in a round until now (inner nodes only)
    private long roundWithLeafMax;  //the round in which leafValuesMax occurs
    private int leafValuesMin;      //the Minimal number of Leaf values calculated in a round until now (inner nodes only)
    private long roundWithLeafMin;  //the round in which leafValuesMin occurs
    private int roundLeafValues;    //the leaf values calculated in the current round
    private int blockLeafValues;   //the leaf values calculated in the current block

    private long maxLeafSum;        //all leaf values calculated until now

    private long round;             //the current round

    @Override
    //resets all fields to sane defualts
    public void prepare() throws IOException {
        hashValuesMax = Integer.MIN_VALUE;
        hashValuesMaxBlock = Integer.MIN_VALUE;
        roundWithHashMax = -1;
        hashValuesMin = Integer.MAX_VALUE;
        roundWithHashMin = -1;
        roundHashValues = 0;
        blockHashValues = 0;
        maxHashSum = 0;


        leafValuesMax = Integer.MIN_VALUE;
        leafValuesMaxBlock = Integer.MIN_VALUE;
        roundWithLeafMax = -1;
        leafValuesMin = Integer.MAX_VALUE;
        roundWithLeafMin = -1;
        roundLeafValues = 0;
        blockLeafValues = 0;
        maxLeafSum = 0;

        round = 0;

        if(logger != null){
            logger.init("round","hashValues", "maxHashValuesBlock",  "maxHashValues" , "leafValues", "maxLeafValuesBlock", "maxLeafValues");           //set the logging coloumn names
        }
    }

    @Override
    //one leaf calced increase counter
    public void leafCalculationDone(long leafIndex) {
        roundLeafValues++;
        blockLeafValues++;
    }

    @Override
    //one hash calced increase counter
    public void hashCalculationDone() {
        roundHashValues++;
        blockHashValues++;
    }

    @Override
    //The round is over calc round statistik and log them
    public void roundFinished(long round) throws IOException {
        this.round = round;
        //Do we have a new hashValuesMax?
        if(roundHashValues > hashValuesMax){
            hashValuesMax = roundHashValues;
            roundWithHashMax =  round;
        }

        if(roundHashValues > hashValuesMaxBlock){
            hashValuesMaxBlock = roundHashValues;
        }

        //Do we have a new hashValuesMin?
        if(roundHashValues < hashValuesMin){
            hashValuesMin = roundHashValues;
            roundWithHashMin =  round;
        }

        //update maxHashSum
        maxHashSum += roundHashValues;

        //Do we have a new leafValuesMax?
        if(roundLeafValues > leafValuesMax){
            leafValuesMax = roundLeafValues;
            roundWithLeafMax =  round;
        }

        if(roundLeafValues > leafValuesMaxBlock){
            leafValuesMaxBlock = roundLeafValues;
        }

        //Do we have a new leafValuesMin?
        if(roundLeafValues < leafValuesMin){
            leafValuesMin = roundLeafValues;
            roundWithLeafMin =  round;
        }

        //update maxLeafSum
        maxLeafSum += roundLeafValues;

        //reset counters
        roundHashValues = 0;
        roundLeafValues = 0;

    }

    @Override
    public void blockFinished(long round) throws IOException {

        if(logger != null){
            logger.log(round,blockHashValues,hashValuesMaxBlock, hashValuesMax, blockLeafValues, leafValuesMaxBlock, leafValuesMax);    //log the round results
        }

        leafValuesMaxBlock = Integer.MIN_VALUE;
        hashValuesMaxBlock = Integer.MIN_VALUE;

        blockLeafValues = 0;
        blockHashValues = 0;
    }

    @Override
    public TimeStatistics genResult() throws IOException {
        double avgHashes =  maxHashSum / ((double)round);       //calculate averageHashes
        double avgLeafes =  maxLeafSum / ((double)round);       //calculate averageLeafes
        if(logger != null) logger.finish();                     //clean up logger
        return new TimeStatistics(                              //Create result
                hashValuesMax, roundWithHashMax,
                hashValuesMin,roundWithHashMin,
                avgHashes,
                leafValuesMax, roundWithLeafMax,
                leafValuesMin, roundWithLeafMin,
                avgLeafes,
                round
        );
    }
}
