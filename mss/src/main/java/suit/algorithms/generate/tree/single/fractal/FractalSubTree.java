package suit.algorithms.generate.tree.single.fractal;


import suit.algorithms.generate.tree.AuthEntry;
import suit.algorithms.interfaces.ICryptPrng;
import suit.interfaces.IAuthEntry;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.SpaceMeasureable;

/**
 * Fractal Merkle Subtree algorithm
 * Parameterized by h and minLevel
 */
public class FractalSubTree  implements SpaceMeasureable {

    @Override
    public void unMark() {
        if(producer!=null)producer.unMark();
        for(Object o : buffer){
            if(o instanceof DataArray) RefCountDataArray.prepare((DataArray)o);
        }
        for(DataArray data : tree)RefCountDataArray.prepare(data);
        if(leafPrng != null) leafPrng.unMark();
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        if(producer!=null)sum+=producer.markAndCount();
        for(Object o : buffer){
            if(o instanceof DataArray) sum+=RefCountDataArray.count((DataArray)o);
        }
        for(DataArray data : tree)sum+=RefCountDataArray.count(data);
        if(leafPrng != null) sum += leafPrng.markAndCount();

        return sum;
    }

    protected byte minLevel;                  //minimum level of nodes we have to store

    private int leaveTreeIndex = 0;         //leaf index, but local to the current subtree: is needed to calculate which nodes can savely be freed, and when no more hashes are needed
    ICryptPrng leafPrng = null;               //prng to use for leaf calculation

    protected TreeHashGen producer = null;    //the TreHash which produces the nodes of this tree (classical TreeHash)
    protected final FractalTree master;       //the FractalTree to which this subtree belongs

    private boolean nodeSwitched = false;     //defines if siblings are switched or not    //false = parentTree, true = authTree  (allows for compact node storage)

    //Ring buffer (only works up to height 128)
    private byte bufferIndex = 0;
    private byte bufferSize = 0;
    private final Object[] buffer;            //because subtree node releasing and desired tree node aquirng are out of sync, because it can hold nodes (desired ahead) or int indexes (exist ahead) its an Object

    //if positiv, it is the next free classic slot
    protected int freeSlots = 0;             //tracks free slots in subtree (or init index during initialisation)

    //the hole tree
    private final DataArray[] tree;          //the subtree  (its not classical tree indexed but instead in treehash gen order, first node gen index 0 etc...(if nodeSwitched == false, its in authpath release order))


    //called by FractalTree
    public FractalSubTree(byte minLevel, byte height, FractalTree master) {
        assert(height <= 30);                           //we are limited by the int index (long would be to much, this is just a subtree not the hole tree)
        this.minLevel = minLevel;
        this.master = master;
        this.buffer = new Object[height];               //buffer must be of subtree height (proven in paper of fractal tree)
        this.tree = new DataArray[(1 << height+1)-2];   //init tree with nulls
    }

    public void setCprng(ICryptPrng cprng){               //called from fractal tree to init prng
        assert(leafPrng == null);
        assert(cprng.index() == (1 << (getMaxLevel())));  //is index correct
        this.leafPrng = cprng.capture();                  //copy it
    }

    public int getMaxLevel(){
        return  minLevel+getHeight();
    }

    protected int getHeight(){
        return buffer.length;
    }

    // have to be called in the order in which treeHash gens the nodes   //called by FractalTree
    public void initNextNode(DataArray value, int level){
        assert(level >= minLevel && level < getMaxLevel() && freeSlots < tree.length);
        tree[freeSlots++] = value;
    }

    //called by FractalTree
    public void finishInitialisation(boolean needsProducer){
        assert(freeSlots == tree.length);
        for(DataArray arr:tree) assert(arr != null);        //was everything inited
        freeSlots = 0;                                      //ready for auth mode, no free slots
        //we are not top Level Tree
        if(needsProducer){                                  //if not top level we need treehash for desired tree
            producer = new TreeHashGen(getMaxLevel(),this);
        }
    }

    //Helpers
    //pushes one element into the ring buffer
    private void fillBuffer(Object s){
        buffer[(bufferIndex + bufferSize) % buffer.length] = s;
        assert(bufferSize !=  buffer.length);
        bufferSize++;
    }

    //reads one element from the ring buffer
    private Object readBuffer(){
        assert(!bufferIsEmpty());
        bufferSize--;
        Object res = buffer[bufferIndex++];
        bufferIndex = (byte)(bufferIndex%buffer.length);
        return res;
    }

    //checks if buffer is empty
    private boolean bufferIsEmpty(){
        return bufferSize == 0;
    }


    //generate part of the authPath which resides in this subtree
    public void nextAuth(int leaveIndex, IAuthEntry[] auths){
        //transform leave index to subtreeleave index and then localize it
        int levelLeaveIndex = ((leaveIndex >>> minLevel) & (1 << getHeight())-1)+1;
        //extraction algorithm differs by current mode
        if(nodeSwitched){
            nextAuthSwitched(levelLeaveIndex, auths);
        }else{
            nextAuthNotSwitched(levelLeaveIndex, auths);
        }
    }

    //exist has to become desired and desired has to become new
    private void doTreeSwitch(int leaveIndex){
        assert(bufferIsEmpty());
        assert(freeSlots == 0);
        assert(bufferSize == 0);
        //switch mode
        nodeSwitched = !nodeSwitched;
        leaveTreeIndex = 0;
        producer = null;

        //do  set producer only if we are not last
        if(master.getTotalLeaves() > (leaveIndex + (1 << getMaxLevel()))){
            producer = new TreeHashGen(getMaxLevel(),this);
        } else {
            leafPrng = null;
        }
    }

    //treeModeSpecific Authentification Method
    private void nextAuthSwitched(int levelLeaveIndex, IAuthEntry[] auths){
        int levelIndex = levelLeaveIndex-1;
        int treeIndex = leaveTreeIndex;                         //index of the active node in the tree, it starts with the active nextLeave index in the tree  (with classic indexing not treeHash gen release order)
        int completeCheckMask = 0;                              //this is a mask used for fast % 2^n  operations, it is always 2^n - 1, so we can use & instead of % for Modulo
        //we have one auth node per level so go through them
        for(byte level = 0; minLevel + level < auths.length && level < getHeight(); level++){
            //Level offset for right kids is: (2 << level)
            DataArray hash =   tree[treeIndex];
            if ((levelLeaveIndex & completeCheckMask) == 0) {   //check if all kids of current Node were already used
                freeSlot(treeIndex);                            //the corresponding authentication node is no longer needed in future authentication
                leaveTreeIndex++;                               //the next tree index was given to this node so the index for the next nextLeave increases by one more
            }

            //calc index for next level
            if((levelIndex & 1)==0) {                                      //we are a left child
                auths[minLevel + level] = new AuthEntry(hash,false);       //Store the hash to the auth path classic (leftSilbling is false, because interlal rep is inverted @ the moment) <-- projection into outside view
                treeIndex += (2 << level);                                 // Up a level
            } else {                                                       //we are a right child
                auths[minLevel + level] = new AuthEntry(hash,true);        //Store the hash to the auth path classic (leftSilbling is true, because interlal rep is inverted @ the moment) <-- projection into outside view
                treeIndex += 1;                                            // Up a level
            }

            //track them instead of recalcing them
            completeCheckMask = ((completeCheckMask << 1) | 1);    //increase modulo factor (mask) for check if all kids of current Node are done (next level needs double the processed nextLeave then the current to be done)

            levelIndex = levelIndex >>> 1 ;                        //divide by to rounds down, to get index of the node in the next level
        }
    }

    //treeModeSpecific Authentification Method
    private void nextAuthNotSwitched(int levelLeaveIndex, IAuthEntry[] auths){
        int levelIndex = levelLeaveIndex-1;                     //position of the active node in the current level (used to decide if we are a left or right child)

        int treeIndex = leaveTreeIndex;                         //index of the active node in the tree , it starts with the active nextLeave index in the tree   (with classic indexing not treeHash gen release order)
        int completeCheckMask = 0;                              //this is a mask used for fast % 2^n  operations, it is always 2^n - 1, so we can use & instead of % for Modulo
        //we have one auth node per level so go through them
        for(byte level = 0; minLevel + level < auths.length && level < getHeight(); level++){
            int authIndex;                                      //index of the authentication node for the active node
            //calc index for next level & for authNode
            boolean isAuthleftChild;

            if((levelIndex & 1)==0) {                           //we are a left child
                    treeIndex += (2 << level);                  // Up a level
                    authIndex =  treeIndex -1;                  // index transformation to get right Sibling (one level up (+offset, already done), and then down to the right child (-1))
                    isAuthleftChild = false;
            } else {
                    treeIndex += 1;                             // Up a level
                    authIndex =  treeIndex -(2 << level);       // index transformation to get left Sibling (one level up (+1, already done), and then down to the left child (-offset))
                    isAuthleftChild = true;
            }

            auths[minLevel + level] = new AuthEntry(tree[authIndex],isAuthleftChild);  //Store the hash to the auth path classic

            if ((levelLeaveIndex & completeCheckMask) == 0) {   //check if all kids of current Node were already used
               freeSlot(authIndex);                             //the corresponding authentication node is no longer needed in future authentication
               leaveTreeIndex++;                                //the next tree  index was given to this node so the  index for the next nextLeave increases by one more
            }
            //track them instead of recalcing them
            completeCheckMask = ((completeCheckMask << 1) | 1); //increase modulo factor (mask) for check if all kids of current Node are done (next level needs double the processed nextLeave then the current to be done)

            levelIndex = levelIndex >>> 1 ;                     //divide by to rounds down, to get index of the node in the next level
        }
    }

    //free a slot for the filler
    private void freeSlot(int index){
        if(producer == null){                           //desire is not avaiable
            tree[index] = null;                         //set slot null, so it can be collected
        }else if (freeSlots > 0 || bufferIsEmpty()){    //are their already free slots queued (exist is ahead) or we are even, then we cann buffer it
            assert(index >= 0 && index < tree.length);
            fillBuffer(index);                          //put index into buffer
            freeSlots++;
            tree[index] = null;                         //set slot null, so it can be collected
        } else {                                        //elements waiting to be filled
            DataArray val = (DataArray)readBuffer();    //take one from the buffer
            tree[index] = val;                          //and put it in to the tree
        }
    }

    //Tree Building
    //is called from FractalTree
    public void buildStep(int leaveIndex){
        //Skip if no desired tree
        if(producer != null){
            //Make two steps
            producer.singleStep();
            master.measureDynamicSpace();              //measure
            producer.singleStep();
            master.measureDynamicSpace();              //measure
            //do we need to switch trees?
            if((leaveIndex % (1 << getMaxLevel())) == 0){
                assert(leaveTreeIndex == tree.length);
                doTreeSwitch(leaveIndex);
            }
        }
        master.measureDynamicSpace();                   //measure
    }

    //called by TreeHashGen
    public void provide(DataArray value, int level) {
        if(level >= minLevel){                          //is it already on a level we have to store
            if(freeSlots > 0){                          //do we have freeSlots
                int idx = (int)readBuffer();            //read free slot index
                tree[idx] = value;                      //store it there
                freeSlots--;
            } else {
                fillBuffer(value);                      //buffer it (no free slots yet)
            }
        }
    }

    //called by TreeHashGen
    public DataArray nextLeaf() {
        DataArray res = master.calcLeave(leafPrng);     //calc next leaf
        leafPrng.next();                                //advance prng
        return res;
    }

    //called by TreeHashGen
    public DataArray calcInnerNode(DataArray left, DataArray right) {
        return master.calcInnerNode(left, right);
    }




}
