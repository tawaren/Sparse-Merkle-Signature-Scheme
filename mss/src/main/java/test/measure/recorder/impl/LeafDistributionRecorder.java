package test.measure.recorder.impl;

import test.measure.logger.ILogger;
import test.measure.recorder.ARecorder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Measures how many times each leaf is calculated (without initialisation)
 * (if a logger is provided the round datas are pushed to the logger in addition)
 */
public class LeafDistributionRecorder extends ARecorder<DistributionStatistics> {
    //the backing logger (null if none)
    private ILogger logger;
    private long maxLeaf = 0;

    //Constructor with Logger
    public LeafDistributionRecorder(ILogger logger) {
        this.logger = logger;
    }

    //Constructor without Logger
    public LeafDistributionRecorder() {
        this.logger = null;
    }



    @Override
    public boolean needsLeafData() {
        return true;
    }



    //accumulator for each Leaf
    Map<Long,Integer> data = new HashMap<>();


    @Override
    public void prepare() throws IOException {
        maxLeaf = 0;
        data.clear();                                   //reset accumulator
        if(logger != null){
            logger.init("leafIndex","computations");    //set the logging column names
        }
    }

    @Override
    //record a leaf calc
    public void leafCalculationDone(long leafIndex) {
        if(leafIndex > maxLeaf)maxLeaf = leafIndex;
        if(data.containsKey(leafIndex)){
           //if Key already exist increase it
           data.put(leafIndex,data.get(leafIndex)+1);
        }else {
           //if key not exist set its value 1
           data.put(leafIndex,1);
        }
    }


    @Override
    public DistributionStatistics genResult() throws IOException {
       //log all results (is not possible earlier)
       if(logger != null){
           for(long i = 0; i <= maxLeaf; i++){
               if(data.containsKey(i)){
                   logger.log(i,data.get(i));
               } else {
                   logger.log(i,0);
               }
           }
           logger.finish();     //clean up logger
       }
       return new DistributionStatistics(data);  //create result
    }
}
