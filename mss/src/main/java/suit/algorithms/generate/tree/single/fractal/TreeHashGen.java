package suit.algorithms.generate.tree.single.fractal;

import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.SpaceMeasureable;


/**
 * TreeHash (the classical, not the improved version)
 */
public final class TreeHashGen implements SpaceMeasureable {

    @Override
    public void unMark() {
        for (DataArray a: stack) RefCountDataArray.prepare(a);
        RefCountDataArray.prepare(pending);
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        for (DataArray a: stack) sum += RefCountDataArray.count(a);
        sum += RefCountDataArray.count(pending);
        return sum;
    }

    private final DataArray[] stack;            //stack as array
    private DataArray pending;                  //active node (instead of cluttering top of stack)
    private int pendingLevel;                   //level of active node
    private final int maxHeight;                //height of root of tree to calc

    private FractalSubTree controller;          //the consumer

    public TreeHashGen(int maxHeight, FractalSubTree controller) {
        this.maxHeight = maxHeight;
        this.controller = controller;
        this.stack = new DataArray[maxHeight];
        this.pending = null;
        this.pendingLevel = -1;
    }

    //2^(N+1)-1 <-- calls to this must ensure, that root is avaiable
    public boolean singleStep(){
        if (pendingLevel == maxHeight) return false;                    //are we already finished?
        DataArray res;
        if(pending == null){                                            //have we an active node
           res = controller.nextLeaf();                                 //No so calc one (leaf)
           if(stack[0] == null){                                        //is their something on the stack with level 0
               stack[0]= res;                                           //No so put it there
               pendingLevel = 0;
           } else {
               pending = res;                                           //yes, put it in pending
           }
        } else {                                                        //we have a pending
          res = controller.calcInnerNode(stack[pendingLevel], pending);   //calc inner node
          stack[pendingLevel] = null;                                   //release needed child
          pending = null;                                               //release pending
          pendingLevel++;                                               //increase level
          if(pendingLevel == maxHeight){                                //are we already finished?
              return false;
          } else if(stack[pendingLevel] == null){                       //is their already a node with this level
              stack[pendingLevel] = res;                                //No, so put it
          } else {
              pending = res;                                            //Yes, use it as pending
          }
        }
        controller.provide(res,pendingLevel);                           //process the newly generated node
        return true;
    }

    //Does all steps and return root
    public DataArray init() {
        while (singleStep()){}
        return getRootHash();
    }

    //check if already done
    public boolean hasFinished() {
        return pendingLevel == maxHeight;
    }

    //Get the calced value (roor hash)
    public DataArray getRootHash() {
        if(hasFinished()){
            return pending;
        }
        throw new IllegalStateException();
    }
}
