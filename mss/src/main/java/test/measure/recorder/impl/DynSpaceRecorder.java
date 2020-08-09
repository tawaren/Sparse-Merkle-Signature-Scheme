package test.measure.recorder.impl;


import test.measure.logger.ILogger;
import test.measure.recorder.ARecorder;

import java.io.IOException;

/**
 * Measures Hash values stored and creates a SpaceStatistics
 * (if a logger is provided the round datas are pushed to the logger in addition)
 */
public class DynSpaceRecorder extends ARecorder<SpaceStatistics> {
    //the backing logger (null if none)
    private ILogger logger;

    //Constructor with Logger
    public DynSpaceRecorder(ILogger logger) {
        this.logger = logger;
    }

    //Constructor without Logger
    public DynSpaceRecorder() {
        this.logger = null;
    }

    @Override
    public boolean needsDynamicSpaceData() {
        return true;
    }

    @Override
    public boolean needsRoundData() {
        return true;
    }

    private int tMaxSpace;          //Max hash values stored over all rounds until now
    private long roundWithMax;      //the round in which tMaxSpace occurs
    private int tMinSpace;          //Min hash values stored over all rounds until now
    private long roundWithMin;      //the round in which tMinSpace occurs

    private long maxSum;            //sum over the max values occured in each round
    private long minSum;            //sum over the min values occured in each round

    private long round;             //current round

    private int cMaxSpace;          //current max value (max value in current round up to now)
    private int cMinSpace;          //current min value (min value in current round up to now)


    @Override
    public void prepare() throws IOException {
        tMaxSpace = Integer.MIN_VALUE;
        cMaxSpace = Integer.MIN_VALUE;
        roundWithMax = -1;
        maxSum = 0;

        tMinSpace = Integer.MAX_VALUE;
        cMinSpace = Integer.MAX_VALUE;
        roundWithMin = -1;
        minSum = 0;

        round = 0;
        if(logger != null){
            logger.init("round","minSpace", "maxSpace");            //set the logging column names
        }
    }

    @Override
    //The round is over calc round statistik and log them
    public void blockFinished(long round) throws IOException {
        this.round = round;
        //Do we have a new tMaxSpace?
        if(tMaxSpace < cMaxSpace){
            tMaxSpace = cMaxSpace;
            roundWithMax =  round;
        }

        //Do we have a new tMinSpace?
        if(tMinSpace > cMinSpace){
            tMinSpace = cMinSpace;
            roundWithMin =  round;
        }

        //update maxSum/minSum
        maxSum +=  cMaxSpace;
        minSum +=  cMinSpace;

        if(logger != null){
            logger.log(round, cMinSpace, cMaxSpace);     //log the round results
        }

        //reset round values
        cMaxSpace = Integer.MIN_VALUE;
        cMinSpace = Integer.MAX_VALUE;

    }

    @Override
    public void dynamicSpaceMeasurementDone(int space) {
        //is this a new max in this round?
        if(cMaxSpace < space){
            cMaxSpace = space;
        }

        //is this a new min in this round?
        if(cMinSpace > space){
            cMinSpace = space;
        }
    }

    @Override
    public SpaceStatistics genResult() throws IOException {
        double avgMax =  maxSum / ((double)round);       //calculate average maximum storage
        double avgMin =  minSum / ((double)round);       //calculate average maximum storage
        if(logger != null) logger.finish();              //clean up logger
        //Create result
        return new SpaceStatistics(tMaxSpace, roundWithMax, tMinSpace, roundWithMin, avgMax, avgMin, round);
    }
}
