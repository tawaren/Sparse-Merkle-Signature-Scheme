package suit.algorithms.generate.tree.single.advanced;


import suit.algorithms.generate.tree.interfaces.INodeCalcController;
import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.SpaceMeasureable;

/**
 * TreeHash algorithm variant which shares a common Stack
 */
public final class SharedTreeHash implements SpaceMeasureable {

    @Override
    public void unMark() {
        if(stack != null) for (DataArray arr: stack) RefCountDataArray.prepare(arr);
        RefCountDataArray.prepare(node);
        if(activePRNG != null) activePRNG.unMark();
        if(nextPRNG != null) nextPRNG.unMark();

    }

    @Override
    public int markAndCount() {
        int sum = 0;
        if(stack != null) for (DataArray arr: stack) sum += RefCountDataArray.count(arr);
        sum += RefCountDataArray.count(node);
        if(activePRNG != null) sum += activePRNG.markAndCount();
        if(nextPRNG != null) sum += nextPRNG.markAndCount();
        return sum;
    }


    private final byte level;               //level of the root of the calculated tree
    private ICryptPrng activePRNG;
    private ICryptPrng nextPRNG;
    private INodeCalcController master;     //Consumer of the results
    private DataArray node;                 //root node if calculated else null (need to be hold outside)
    private final DataArray[] stack;        //shared stack
    private byte tailHeight;                //if negative: -1 means global stack is empty, -2 means finished, if positiv it means the current tailHeight in stack


    protected SharedTreeHash(final byte level, final INodeCalcController master, final DataArray[] stack) {
        assert(level < 32);
        this.level = level;
        this.stack = stack;
        this.node = null;
        this.master = master;
        this.tailHeight =-2;
    }

    public void setCprng(ICryptPrng cprng){               //called from fractal tree to init prng
        assert(nextPRNG == null);
        assert(cprng.index() == 3*(1 << (level)));  //is index correct
        this.nextPRNG = cprng.capture();                  //copy it
    }

    public void advanceNextPrng(){
        nextPRNG.next();
    }

    //Consumes the calced root and prepare for calculating the next node on that level
    public DataArray getAndRestart(long index){
        assert(node != null);
        DataArray tmpNode = node;                   //fetch res
        tailHeight = level;                         //reset tail height
        node = null;                                //release root
        activePRNG = nextPRNG.capture();
        assert(index == activePRNG.index());
        return tmpNode;                             //return res
    }

    //returns the tail height (level of node with smallest level)
    public int getTailHeight(){
        return tailHeight;
    }

    //checks if this has not finished
    public boolean isActive(){
        return tailHeight > -2;
    }

    //loo at top node
    private DataArray peek(final int level){
        return stack[level];
    }

    //pops top node from shared stack
    //assumes that it is followed by a push or tailHeight reset (in other cases tailHeight may be wrong)
    private DataArray pop(final int level){
        final DataArray val = stack[level];
        stack[level] = null;
        assert(val != null);
        return val;
    }

    //set root node and mark notActive
    public void finishWith(final DataArray value){
        assert(node == null);
        node = value;
        tailHeight = -2;
    }

    //put node on shared stack
    private void push(final DataArray val, final int level){
        assert(stack[level] == null);
        stack[level] = val;
        tailHeight = (byte)level;
    }

    //make a update on the shared treeHash
    public void update(){
        assert(isActive());
        //assert(calcIndex >= 0);
        DataArray value = master.calcLeave(activePRNG);
        activePRNG.next();
        treeHash(value);
    }

    private void treeHash(DataArray value){
        int level = 0;                                          //level of active node
        assert(node == null);                                   //assert we are not finished
        while(level < this.level && peek(level) != null){       //is the top node of same height and are we not finished
            value = master.calcInnerNode(pop(level++), value);   //calculate parent node
        }

        if(level == this.level){                                //was it the last?
            finishWith(value);                                  //then finish
        } else {
            assert(peek(level) == null);
            push(value, level);                                 //else simply push
        }
    }

}
