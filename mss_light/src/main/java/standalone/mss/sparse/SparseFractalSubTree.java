package standalone.mss.sparse;


import standalone.prng.Prng;

import static standalone.utils.TreeNavigationUtils.*;

/**
 * Sparse Merkle Subtree algorithm
 * Parameterized by h and minLevel
 */
public final class SparseFractalSubTree {
    private final byte[][] tree;                  //the stored nodes (the subtree, but only right nodes)
    private final byte[][] stackHigh;             //the shared higher stack (stackHigh[getMaxLevel()-1] holds the result of the lower treeHash)
    private final SparseFractalTree master;       //the FractalTree to which this subtree belongs

    private final byte minLevel;                  //minimum level of nodes we have to store
    private final byte maxLevel;                  //maximum level of nodes we have to store

    private long desiredLeafIndex;                //the next leaf index needed to calc the desired tree (-2 indicates fully finished, no further leaves needed)
    Prng cprng = null;                            //prng to use for leaf calculation
    private final byte[][] stackLow;              //the shared lower stack (stackLow[minLevel] is in stackHigh[getMaxLevel()-1] when finished)
    private byte initState;                       //if negative: -1 means global stack is empty, -2 means stopped (global&local empty), if positiv it represents the current tailHeight in stackLow

    //Constructor
    protected SparseFractalSubTree(final byte minLevel, final byte height, final SparseFractalTree master, final byte[][] stackLow, final byte[][] stackHigh) {
        assert(height < 32);
        this.minLevel = minLevel;
        this.maxLevel = (byte)(minLevel+height);
        this.master = master;
        this.desiredLeafIndex = leavesInTreeWithHeight(maxLevel) -1;   //index before the leaf needed for desired tree
        this.tree = new byte[(int)nodesInTreeWithHeight(height)][];    //create empty subtree
        if(maxLevel == master.getHeight()){                            //if this is top no stacks are needed
            this.stackHigh = null;
            this.stackLow = null;
        }else {
            //store the shared stacks
            this.stackHigh =  stackHigh;
            assert(stackLow.length >= minLevel);
            this.stackLow = stackLow;
        }
        this.initState = minLevel;
    }

    public void setCprng(Prng cprng){
        assert(cprng.index() == desiredLeafIndex +1);  //is index correct
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
        return desiredLeafIndex == -2;
    }

    public int getTailHeight(){
        return initState;
    }

    public byte getMaxLevel() {
        return maxLevel;
    }

    //Finds out if the next leave does only contribute to the first node of the tree
    // (as this is a left node that never contributes to a non left node in the tree, itd does not need to be computed)
    public boolean isEarly(){
        //check if the next leaf would not contribute to the calculation of any right node with a level between min and max level
        //First find the leave index localized to this subtree
        long localIndex = shiftIndexIntoTree(desiredLeafIndex +1, 0, maxLevel);
        //Next get the number of leaves that are children of the first node in this subtree
        return localIndex < leavesInTreeWithHeight(minLevel);
    }

    //helper for the stacks
    private byte[] peekHigh(final int level){
        return stackHigh[level];
    }

    private byte[] peekLow(final int level){
        return stackLow[level];
    }

    //assumes that it is followed by a push or initState reset (in other cases initState may be wrong)
    private byte[] popHigh(final int level){
        byte[] val = stackHigh[level];
        stackHigh[level] = null;
        assert(val != null);
        return val;
    }

    private byte[] popLow(final int level){
        final byte[] val = stackLow[level];
        stackLow[level] = null;
        assert(val != null);
        return val;
    }

    private void pushHigh(final byte[] val, final int level){
        assert(stackHigh[level] == null);
        assert(val != null);
        stackHigh[level] = val;
    }

    private void pushLow(final byte[] val, final int level){
        assert(stackLow[level] == null);
        assert(val != null);
        stackLow[level] = val;
        initState = (byte)level;     //adapt the tail height (it has changed with the push)
    }

    //computes the tree index from level and the index on that level
    private int calcIndex(final int level, final int index){
        assert( level >= 0 && level < getHeight());
        assert( index >= 0 && index < leavesInTreeWithHeight(getHeight()));
        //levels above (these are stored earlier in the array)
        int numberOfHigherLevels = getHeight()-level;
        //Number of nodes stored on higher levels then desired Level
        //Formula for nodes in a tree is n-1 (where n is number of leaves)
        //Formula for number of leaves is 2^h (which is 1 << h)
        //         we divide by 2 as we only store right nodes: 2^(h-1)
        //Formula for nodes on higher level: (2^(h-1)) -1
        int nodesOnHigherLevel = (1 << (numberOfHigherLevels-1)) -1;
        //we divide by 2 as we do not store the left nodes
        int levelLocalIndex = (index >>> 1);
        //calc the index
        return nodesOnHigherLevel + levelLocalIndex;
    }

    //sets node during initialisation
    public void initNode(final byte[] value, final int localLevel, final int levelIndex){
        assert(isRightNode(levelIndex));                         //is right node
        assert(localLevel >= 0 && localLevel <= getHeight());    //is in subtree
        int index = calcIndex(localLevel,levelIndex);
        assert(tree[index] == null);                             //is not already set
        tree[index] = value;                                     //set it
    }

    //gets a rightNode
    public byte[] getRightByIndex(final int globalLevel, final int levelIndex){
        assert(globalLevel >= 0);
        assert(levelIndex >= 0);
        byte[] val = tree[(calcIndex(globalLevel-minLevel,levelIndex))];   //fetch It
        assert(val != null);                                                    //is available
        return val;
    }

    //releases a rightNode
    public void releaseRightByIndex(final int globalLevel, final int levelIndex){
        assert(globalLevel >= 0);
        assert(levelIndex >= 0);
        int index = calcIndex(globalLevel-minLevel,levelIndex);
        assert(tree[index] != null);    //is not already released
        tree[index] = null;             //release it
    }

    public void updateTreeHashLow(){
        //assert that the previously computed leaf node was consumed
        assert(initState < Byte.MAX_VALUE);
        //we never compute the first leaf node in subtree, as it never contributes to anything we need
        //is early checks if we currently are in the process of computing that leaf node
        if(isEarly()) {
            //just do nothing
            desiredLeafIndex++;
            //if this was the last early we need to signal that the first leaf node is computed (even if its not)
            if(!isEarly()) {
                //this is highLeaf = dummy (used as signalling)
                initState = Byte.MAX_VALUE;
            }
        } else {
            desiredLeafIndex++;                //increment the index
            treeHashLow();                     //update the lower TreeHash
        }
        cprng.next();                          //advance the Sk generator
    }

    //makes one update to the tree hash low
    private void treeHashLow(){
        assert(stackHigh[getMaxLevel()-1] == null); //ensure the leave is not already fully computed
        assert(desiredLeafIndex == cprng.index());  //ensure the desiredLeafIndex is aligned with the leave generator
        byte[] value = master.calcLeaf(cprng);    //calc the next leaf
        //propagate it up, hashing it with the right nodes on the stack until we find a free spot to put it
        //we start at the bottom
        int level = 0;
        //we stop at minLevel because that is the leave we are computing (which then would go into the stackHigh)
        //as long we have a rightNode on the active level in the stack we remove it and compute the parent and then continue with it
        while(level < minLevel && peekLow(level) != null){
            byte[] node = popLow(level++);               //Get the right node
            value = master.calcInnerNode(node, value);   //hash nodes together
        }

        //we have found an empty slot / non computed node
        //check if it is a leave of this subtree or still on the stack low
        if(level == minLevel){
            //we have finished - is a node of this subtree
            pushHigh(value, getMaxLevel()-1);      //store the finish value outside the stackLow (on stack high)
            initState = Byte.MAX_VALUE;                 //set tailHeight correctly (we have no more node on low, thus set it to max)
        } else {
            assert(peekLow(level) == null);             //this slot should be free
            pushLow(value, level);                      //push onto stackLow (will set tailHeight to Level)
        }
    }

    public void updateTreeHashHigh(){
        assert(!hasStopped());                  //ensure we are still acive
        assert(initState == Byte.MAX_VALUE);    //ensure the treeHash low has produced a leave

        //now we can process the leafTreeHashHigh because its spaceMax is free
        if(treeHashHigh() == getMaxLevel()-1 )  { //check if we reached the top
            assert(initState == minLevel);
            stopCheck();                          //stop if necessary
        }
    }

    //is the fix stack algo, which starts on higher level
    private int treeHashHigh(){
        //we can not use pop here as value may be null
        byte[] value = stackHigh[getMaxLevel()-1];
        stackHigh[getMaxLevel()-1] = null;
        //ensure we have a new leave for this tree hash computed
        assert(initState == Byte.MAX_VALUE);
        //if so we set it in motion by setting tail hight to min level
        initState = minLevel;                 //bring lower TreeHash back into the game
        //check if this is a dummy that was set in the special isEarly case
        //if it easy we don't have to do anything and are done
        if (value == null) return minLevel;
        //if we have a value start computing nodes until we can no more
        int level = minLevel;                                       //start on min level (where we just got a new leave (in value))

        //Calculate the index of the parent node of desiredLeafIndex which is in this tree (this will be the index of value)
        long bottomIndex = shiftIndexIntoTree(desiredLeafIndex, 0, maxLevel);
        int levelIndex = (int) ancestorLevelIndex(bottomIndex, level);
        //process the newly computed node: if we no longer require it (processNode returns ture) we are done
        if (processNode(level, value, levelIndex)) return level;
        //compute inner nodes until we reach an empty stack slot or we know that we will never require any of the nodes parents anymore
        while(peekHigh(level) != null){
            //ensure we are a rightNode on that level (the stack provides the leftNodes)
            assert(isRightNode(ancestorLevelIndex(desiredLeafIndex,level)));
            //we have a sibling so compute the parent
            value = master.calcInnerNode(popHigh(level++), value);
            //we can shorten the new index computation, we need just to divide the previous by 2 to get the parens level index
            levelIndex = (int)parentLevelIndex(levelIndex);
            //process the newly computed node: if we no longer require it (processNode returns ture) we are done
            if (processNode(level, value, levelIndex)) return level;
        }
        //ensure the target slot is free
        assert(peekHigh(level) == null);
        assert(level != getHeight() -1 || isRightNode(ancestorLevelIndex(desiredLeafIndex,level)));
        pushHigh(value, level);                                                  //push the node back
        return level;                                                            //return level for finish check
    }

    //returns true if this is the end and we do not need any of its parents
    private boolean processNode(final int level, final byte[] value, final int levelIndex ){
        assert(level >= minLevel);  //ensure its at least in our subtree
        assert(value != null);
        if(isRightNode(levelIndex)){                                //is it a right node (we only have to store right nodes)
            int relLevel = level-minLevel;                          //compute the level relative to minLevel
            int index = calcIndex(relLevel,levelIndex);
            assert(tree[index] == null);                            //ensure the slot is free
            tree[index] = value;                                    //store it into the tree
            if(levelIndex == 1){                                    //If this is the first rightNode on a level than all its parents (in this tree) are left nodes (and we do not need them)
                assert(initState == minLevel);                      //this should only happen when the stack high os otherwise empty (else we would need the node to compute the siblings of nodes on the stack)
                return true;                                        //this has no right node parent (break -  and signal to caller this fact)
            }

        }
        return false;                                               //this is a left node (it must have a right node parent or would never have been computed)
    }

    public final void stopCheck(){
        //Are we the last leaf
        long numLevelZeroLeaves = leavesInTreeWithHeight(master.getHeight());     //How many are their
        if(desiredLeafIndex+1 == numLevelZeroLeaves){               //+1 as index starts at 0
            //We are, so there should be no more on the stack else we would need siblings for them
            assert(initState == minLevel);
            assert(desiredLeafIndex == -2 || stackHigh == null|| stackHigh[getMaxLevel()-1] ==  null);
            desiredLeafIndex = -2;     //mark finished
            cprng = null;              //release cprng (no longer needed)
        }
    }

}
