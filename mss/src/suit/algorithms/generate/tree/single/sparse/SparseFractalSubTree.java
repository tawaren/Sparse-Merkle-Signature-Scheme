package suit.algorithms.generate.tree.single.sparse;


import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.SpaceMeasureable;

/**
 * Sparse Merkle Subtree algorithm
 * Parameterized by h and minLevel
 */
public final class SparseFractalSubTree implements SpaceMeasureable {


    @Override
    public void unMark() {
        for (DataArray arr: tree) RefCountDataArray.prepare(arr);
        if(stackHigh != null)for (DataArray arr: stackHigh) RefCountDataArray.prepare(arr);
        if(stackLow != null) for (DataArray arr: stackLow) RefCountDataArray.prepare(arr);
        if(cprng != null)cprng.unMark();
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        for (DataArray arr: tree) sum += RefCountDataArray.count(arr);
        if(stackLow != null) for(int i = minLevel ; i < maxLevel && i < stackLow.length; i++) sum += RefCountDataArray.count(stackLow[i]);
        if(stackHigh != null) for(int i = minLevel ; i < maxLevel-1; i++) sum += RefCountDataArray.count(stackHigh[i]);
        if(stackHigh != null) sum += RefCountDataArray.count(stackHigh[maxLevel-1]);
        if(cprng != null) sum += cprng.markAndCount();

        return sum;
    }

    private final DataArray[] tree;               //the stored nodes (the subtree, but only right nodes)
    private final DataArray[] stackHigh;          //the shared higher stack (stackHigh[getMaxLevel()-1] holds the result of the lower treeHash)
    private final SparseFractalTree master;       //the FractalTree to which this subtree belongs

    private final byte minLevel;                  //minimum level of nodes we have to store
    private final byte maxLevel;                  //maximum level of nodes we have to store

    private long desireLeafIndex;                 //the next leaf index needed to calc the desired tree (-2 indicates fully finished, no further leaves needed)
    ICryptPrng cprng = null;                      //prng to use for leaf calculation
    private final DataArray[] stackLow;           //the shared lower stack (stackLow[minLevel] is in stackHigh[getMaxLevel()-1] when finished)
    private byte initState;                       //if negative: -1 means global stack is empty, -2 means stopped (global&local empty), if positiv it means the current tailHeight in stackLow

    //Constructor
    protected SparseFractalSubTree(final byte minLevel, final byte height, final SparseFractalTree master, final DataArray[] stackLow, final DataArray[] stackHigh) {
        assert(height < 32);
        this.minLevel = minLevel;
        this.maxLevel = (byte)(minLevel+height);
        this.master = master;
        this.desireLeafIndex = (1 << maxLevel)-1;   //index of first leaf needed for desired tree
        this.tree = new DataArray[(1 << height)-1]; //create empty subtree
        if(maxLevel == master.getHeight()){         //if this is top no stacks are needed
            this.stackHigh = null;
            this.stackLow = null;
        }else {
            //store the shared stack s
            this.stackHigh =  stackHigh;
            assert(stackLow.length >= minLevel);
            this.stackLow = stackLow;
        }
        this.initState = minLevel;

    }

    public void setCprng(ICryptPrng cprng){
        assert(cprng.index() == desireLeafIndex+1);    //is index correct
        this.cprng = cprng.capture();                  //copy it
    }

    public byte getHeight() {
        return (byte)(maxLevel-minLevel);
    }

    public byte getMinLevel() {
        return minLevel;
    }

    //is only correct if a stopCheck was done after last update or release
    public boolean hasStopped(){
        return desireLeafIndex == -2;
    }

    public int getTailHeight(){
        return initState;
    }

    public byte getMaxLevel() {
        return maxLevel;
    }

    public boolean isEarly(){
        //check if the next leaf would not contribute to the calculation of any right node with a level between min and max level
        return (((desireLeafIndex +1) & ((1 << maxLevel)-1)) < (1 << minLevel));
    }

    //helper for the stacks
    private DataArray peekHigh(final int level){
        return stackHigh[level];
    }

    private DataArray peekLow(final int level){
        return stackLow[level];
    }

    //assumes that it is followed by a push or initState reset (in other cases initState may be wrong)
    private DataArray popHigh(final int level){
        DataArray val = stackHigh[level];
        stackHigh[level] = null;
        assert(val != null);
        return val;
    }

    //assumes that it is followed by a push or initState reset (in other cases initState may be wrong)
    private DataArray popLow(final int level){
        final DataArray val = stackLow[level];
        stackLow[level] = null;
        assert(val != null);
        return val;
    }

    private void pushHigh(final DataArray val, final int level){
        assert(stackHigh[level] == null);
        assert(val != null);
        stackHigh[level] = val;
    }

    private void pushLow(final DataArray val, final int level){
        assert(stackLow[level] == null);
        assert(val != null);
        stackLow[level] = val;
        initState = (byte)level;     //adapt the tail height (it has changed with the push)
    }

    //calcs the tree index from level and the index on that level
    private int calcIndex(final int level, final int index){
        assert( level >= 0 && level < getHeight());
        assert( index >= 0 && index < (1 << getHeight()));
        return (1 << (getHeight()-level-1)) + (index >>> 1) - 1;   //divide by 2, because we store only rightNodes
    }

    //sets node during intitialisation
    public void initNode(final DataArray value, final int localLevel, final int levelIndex){
        assert ((levelIndex & 1) == 1);                          //is right node
        assert(localLevel >= 0 && localLevel <= getHeight());    //is in subtree
        assert(tree[calcIndex(localLevel,levelIndex)] == null);  //is not already set
        tree[calcIndex(localLevel,levelIndex)] = value;          //set it
    }

    //gets a rightNode
    public DataArray getRightByIndex(final int globalLevel, final int levelIndex){
        assert(globalLevel >= 0);
        assert(levelIndex >= 0);
        DataArray val = tree[(calcIndex(globalLevel-minLevel,levelIndex))];     //fetch It
        assert(val != null);                                                    //is avaiable
        return val;
    }

    //releases a rightNode
    public void releaseRightByIndex(final int globalLevel, final int levelIndex){
        assert(tree[(calcIndex(globalLevel-minLevel,levelIndex))] != null);    //is not already released
        tree[(calcIndex(globalLevel-minLevel,levelIndex))] = null;             //release it
    }

    public void updateTreeHashLow(){
        assert(initState < Byte.MAX_VALUE);
        if(isEarly()) {
            desireLeafIndex++;                 //ignore update (is not nice, but excact budgeting does only work in special cases together with stack assumptions)
            if(!isEarly()) {
                //do as if the leaf which is never needed is calced
                initState = Byte.MAX_VALUE;    //this is highLeaf = dummy
            }
        } else {
            desireLeafIndex++;
            assert(desireLeafIndex >= 0);
            treeHashLow();                  //update the lower TreeHash
        }
        cprng.next();                       //advance the Sk generator
    }

    private void treeHashLow(){
        int level = 0;
        assert(stackHigh[getMaxLevel()-1] == null);
        assert(desireLeafIndex == cprng.index());
        DataArray value =  master.calcLeave(cprng);                 //calc the next leaf

        //this suffice, the if we do no stupid stuff everything we calc is already used
        while(level < minLevel && peekLow(level) != null){  //can we continue, ist their a next level which can be calculated and will be used and do not overwrite anything?
            value = master.calcInnerNode(popLow(level++), value);   //hash nodes until no longer possible
        }

        if(level == minLevel){
            //we have finished
            assert(stackHigh[getMaxLevel()-1] == null);
            stackHigh[getMaxLevel()-1] = value;         //store the finish value outside the stackLow
            initState = Byte.MAX_VALUE;                 //set tailHeight correctly
        } else {
            assert(peekLow(level) == null);
            pushLow(value, level);                      //push onto stack
        }
    }

    //need better name removes a lock from a leave
    public void updateTreeHashHigh(){
        assert(!hasStopped());
        assert(initState == Byte.MAX_VALUE);
        //now we can process  the leafTreeHashHigh because its spaceMax is free
        if(treeHashHigh() == getMaxLevel()-1 )  {
            assert(initState == minLevel);
            stopCheck();                   //stop if necessary
        }
    }

    private int treeHashHigh(){          //is the fix stack algo, which  start on higher level and do not have to do try lock
        DataArray value = stackHigh[getMaxLevel()-1];
        stackHigh[getMaxLevel()-1] = null;
        assert(initState == Byte.MAX_VALUE);
        initState = minLevel;                  //bring lower TreeHash back into the game
        if (value ==  null) return minLevel;   //this is the most left leaf and just lead to a dead end
        int level = minLevel;                  //start on minlevel
        int levelIndex = (int)((desireLeafIndex & ((1<<maxLevel)-1))) >>> level;  //level index for process
        if (processNode(level, value, levelIndex)) return level;                  //skip first, this would only calc leftNodes
        //this suffice, the if we do no stupid stuff everything we calc is already used
        while(peekHigh(level) != null){                                           //can we continue, ist their a next level which can be calculated and will be used and do not overwrite anything?
            assert(((desireLeafIndex >>> level) & 1) == 1);
            value = master.calcInnerNode(popHigh(level++), value);                //calc Nodes until no longer possible
            levelIndex >>>= 1;                                                    //adapt level index
            if (processNode(level, value, levelIndex)) return level;              //process Node tells us, that node and its parents will never be used anymore
        }
        assert(peekHigh(level) == null);
        assert(level != getHeight() -1 || ((desireLeafIndex >>> level) & 1) == 1);

        pushHigh(value, level);                                                  //push the node back
        return level;                                                            //return level for finish check
    }

    private boolean processNode(final int level, final DataArray value, final int levelIndex ){
        assert(level >= minLevel);
        assert(value != null);
        assert(levelIndex == ((desireLeafIndex & ((1<<maxLevel)-1)) >>> level));

        if((levelIndex & 1) == 1){                                  //isRightNode
            int relLevel = level-minLevel;                          //relative level
            assert( tree[calcIndex(relLevel,levelIndex)] == null);  //is slot free?
            tree[calcIndex(relLevel,levelIndex)] = value;           //store it
            if(levelIndex == 1){                                    //levelIndex >> 1 == 0 with (levelIndex & 1) == 1 is levelIndex == 1
                assert(initState == minLevel);
                return true;                                        //this has no right node parent  (break)
            }

        }
        return false;                                               //this is a left node (it must have a right node parent or would never be calculated)
    }

    public final void stopCheck(){
        //Are we last leaf
        if(desireLeafIndex +1 == (1L << (master.getHeight()))){
            assert(initState == minLevel);
            assert(desireLeafIndex == -2 || stackHigh == null|| stackHigh[getMaxLevel()-1] ==  null);
            desireLeafIndex = -2;     //mark finished
            cprng = null;
        }
    }

}
