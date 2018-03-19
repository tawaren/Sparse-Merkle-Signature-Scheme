package suit.algorithms.generate.tree.single.sparse;


import suit.algorithms.generate.tree.AuthEntry;
import suit.algorithms.generate.tree.hash.TreeHash;
import suit.algorithms.generate.tree.interfaces.IFullCalcController;
import suit.algorithms.generate.tree.single.SimpleAuthPath;
import suit.algorithms.generate.tree.single.SimpleLeaveAuth;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IMerkleTree;
import suit.config.helper.FractalTreeHeightCalc;
import suit.config.interfaces.ITreeAlgorithmConf;
import suit.interfaces.IAuthEntry;
import suit.interfaces.ILeaveAuth;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.MeasureToolbox;
import test.measure.SpaceMeasureable;

/**
 * Sparse Tree implementation  (mix of Fractal and LogSpaceTime Tree)
 * Algorithm with good space-time trade off (memory footprint like LogSpaceTime Tree)
 * Parameter L (levels) allows for space time trade off
 */
public class SparseFractalTree implements IMerkleTree, IFullCalcController, SpaceMeasureable {

    @Override
    public void unMark() {
        for (SparseFractalSubTree t: subTrees) if(t != null) t.unMark();
        for (IAuthEntry a: auths) if(a != null) RefCountDataArray.prepare(a.getHash());
        if(initializer != null) initializer.unMark();
        if(cprng != null)cprng.unMark();
        RefCountDataArray.prepare(rootHash);
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        for (IAuthEntry a: auths) if(a != null) sum +=  RefCountDataArray.count(a.getHash());
        for (SparseFractalSubTree t : subTrees) if (t != null) sum += t.markAndCount();
        if(initializer != null) sum += initializer.markAndCount();
        if(cprng != null)sum += cprng.markAndCount();
        //sum += RefCountDataArray.count(rootHash);
        return sum;
    }

    public void measureDynamicSpace(){
        if(MeasureToolbox.needsDynamicSpaceData){
            unMark();
            int c = markAndCount();
            MeasureToolbox.emitDynamicSpaceMeasurement(c);
        }
    }

    protected long leaveIndex = 0;                      //in initialisation is needed to trac the next leave for init, in auth mode is needed to trac the current leave
    protected final SparseFractalSubTree[] subTrees;    //the subtrees which store the nodes
    private final IAuthEntry[] auths;                   //current authentification path (is updated by update methode)
    protected final ICryptPrng cprng;                   //prng for calculating private leaf keys
    protected final ITreeAlgorithmConf algs;            //the algorithms to use (hash etc...)

    protected TreeHash initializer;                     //TreeHash for the initialisation and calcing the Pk
    private DataArray rootHash = null;                  //rootHash or initializer should be null never both
    private boolean updateDone = false;                 //flag to cordinate update and next auth


    //Constructor with default level Calc
    public SparseFractalTree(ITreeAlgorithmConf levelConf, int totalHeight, ICryptPrng csprng) {
        this(levelConf, FractalTreeHeightCalc.calcOptHight(totalHeight), csprng);
    }

    //Constructor paramized by levels
    public SparseFractalTree(ITreeAlgorithmConf levelConf, int levels, int totalHeight, ICryptPrng csprng) {
        this(levelConf, FractalTreeHeightCalc.assignHeights(levels, totalHeight),csprng);
    }

    //Constructor witch is paramized by h of each subtree
    public SparseFractalTree(ITreeAlgorithmConf levelConf, byte[] heights, ICryptPrng csprng) {
        //calc height by sum up h's
        int totalHeight = 0;
        for (byte height : heights) totalHeight += height;
        assert(totalHeight < 64);                           //through restriction to long at the moment (if a problem a XMSS scheme must be implemented)
        this.auths = new IAuthEntry[totalHeight];
        this.algs = levelConf;
        assert(csprng.index() == 0);
        this.cprng = csprng.capture();                      //capture the prng to prevent influence fro outside
        this.initializer = new TreeHash(this,csprng.capture(),getHeight());

        byte hCount = 0;
        subTrees = new SparseFractalSubTree[heights.length];
        //prepare global stacks
        DataArray[] globalStackLow;
        DataArray[] globalStackHigh;
        //are the global stacks needed or would they be empty
        if(heights.length > 1){
            globalStackHigh = new DataArray[totalHeight-heights[heights.length-1]];
            globalStackLow = new DataArray[totalHeight-heights[heights.length-1]-heights[heights.length-2]];
        } else {
            globalStackHigh = new DataArray[0];
            globalStackLow = new DataArray[0];
        }

        //initialize the subtrees
        for(int i = 0; i < subTrees.length;i++){
            byte h =  heights[i];
            subTrees[i] = new SparseFractalSubTree(hCount,h, this, globalStackLow,globalStackHigh);
            hCount += h;
        }
    }

    @Override
    public final DataArray getRootPk() {
        if(rootHash == null) finishInitialisation();
        return rootHash;
    }

    public final int getHeight() {
        return auths.length;
    }

    public final int getLevels(){
        return subTrees.length;
    }

    @Override
    public final boolean initStep() {
        assert(initializer != null);
        return initializer.step();       //Do one initialisation step
    }

    public final void init() {
        assert(initializer != null);
        initializer.init();             //do full initialisation
        finishInitialisation();
    }



    protected final void finishInitialisation(){
        assert(rootHash == null);
        assert(initializer != null);
        rootHash = initializer.getRootHash();               //get PK
        initializer = null;                                 //dismiss initializer
        subTrees[subTrees.length-1].stopCheck();            //stop top Subtree
        assert(subTrees[subTrees.length-1].hasStopped());
        leaveIndex = 0;
        updateDone = true;
    }

    //big assert methode to check some assumptions which should hold over all subtreess at any time (expensive, never use in production)
    //checks if the shared stack is consistent
    private boolean check(SparseFractalSubTree reciver){
        boolean reciverPast = false;

        SparseFractalSubTree prev =  reciver;
        for(int i = 0; i < subTrees.length-1; i++){
            if(!reciverPast) {
                if(subTrees[i] == reciver) {
                    //(i+1)*h
                    reciverPast = true;
                } else {
                    //h+1
                    assert (subTrees[i].getTailHeight() >= subTrees[i].getMinLevel()-1);
                }
            } else {
                SparseFractalSubTree subTree =  subTrees[i];
                //2h
                assert(prev.getTailHeight() == Byte.MAX_VALUE ||
                        (
                                (subTree.getTailHeight() >= prev.getTailHeight())  &&
                                (subTree.getTailHeight() >= prev.getMinLevel())
                        )
                );

                prev = subTree;
            }

        }
        return true;
    }

    //shedules a single update
    private void doOneUpdateStep(){
        SparseFractalSubTree target = null;                                     //stores the tree which will recive the update
        for (int i = 0; i < subTrees.length - 1; i++) {                         //top tree never recives an update (reason for -1)
            SparseFractalSubTree subTree = subTrees[i];
                if (!subTree.hasStopped()) {                                    //not permanently or temporary finished
                if (target != null) {                                           //are we the first tree how can recive an update
                    if ((subTree.getTailHeight() < target.getTailHeight())) {   //are we a better candidate then the last one?
                        target = subTree;
                    }
                } else {                                                        //we are the first not finished subtree, so we recive the update if no better candidate is found
                  target = subTree;
                }

            }
        }
        assert (target != null);
        assert (check(target));                 //check all the assumptions

        measureDynamicSpace();                  //measure

        target.updateTreeHashLow();             //apply a update to the target
    }

    private void doBasicUpdate(){
        //distribute one update for every active subtree
        for (int i = 0; i < subTrees.length - 1; i++) {    //top tree never recives an update (reason for -1)
            if(!subTrees[i].hasStopped()) {
                doOneUpdateStep() ;
            }
        }
    }


    @Override
    public final void updateStep() {
        //do nothing if the auth was not consumed (or we can not auth more)
        if(!updateDone && canAuthMore()){
            leaveIndex++;                   //increse the index
            doBasicUpdate();                //do the updates
            measureDynamicSpace();          //measure
            calcNextAuthPath();             //calculate the next authenticaton path
            measureDynamicSpace();          //measure
            updateDone = true;
            if(MeasureToolbox.needsRoundData)MeasureToolbox.finishRound(leaveIndex);    //measure
        }
    }

    private void calcNextAuthPath(){
        //Find needed information for left calc
        int treeIndex = 1;
        SparseFractalSubTree nodeTree = subTrees[0];
        long nodeIndex;
        //calc tau (first level on which auth path changes from right to left node) (or first bit in index where a 0 changes to a 1 on a +1)
        int tau = 0;
        for (nodeIndex = leaveIndex;(nodeIndex & 1) == 0; nodeIndex >>>= 1){
            if(tau++ >= nodeTree.getMaxLevel()) nodeTree = subTrees[treeIndex++];   //track the subtree in which tau is in
        }
        assert (nodeIndex == leaveIndex >>> tau);

        calcLeftNodes(tau, nodeIndex, nodeTree);            //calc the new left auth nodes
        measureDynamicSpace();                              //measure
        releaseRight(tau, treeIndex, nodeIndex, nodeTree);  //release no longer needed right nodes
        measureDynamicSpace();                              //measure
        calcRightNodes(tau);                                //calc the new left auth nodes

    }

    private void calcLeftNodes(int tau, long nodeIndex, SparseFractalSubTree nodeTree){
        //calc left auths
        if(tau == 0){
            //left node is a leaf
            assert(auths[tau] != null);
            auths[tau] = new AuthEntry(calcLeave(cprng.current(),nodeIndex-1),true);   //-1 because leaveIndex is real Path
            cprng.next();
        } else{
            //left node is a inner node
            long childIndex = (leaveIndex >>> tau -1);                      //index of the right child
            //localize right child index
            int localIndex = (int)((childIndex-1) & ((1 << (nodeTree.getMaxLevel()-(tau-1)))-1));  //modulo: for to local index
            DataArray right = nodeTree.getRightByIndex(tau-1,localIndex);   //fetch the right node
            nodeTree.releaseRightByIndex(tau-1, localIndex);                //releaseRightNodes no longer needed for leftCalc (this is whats in keep in orig algo)
            DataArray left = auths[tau-1].getHash();                        //fetch the left node (current auth)
            assert(auths[tau] != null);
            //calc the left node
            auths[tau] = new AuthEntry(calcInnerNode(left,right),true);
        }

    }

    private void releaseRight(int tau, int treeIndex, long nodeIndex, SparseFractalSubTree nodeTree){
        //release right node (sibling of the newly calced left node) if its no longer needed for AuthPath calc
        if((nodeIndex & 2) == 2){                                               //if parent is right node we do not need it anymore (else its needed for left calc)
            if(tau >= nodeTree.getMaxLevel()) nodeTree = subTrees[treeIndex];   //is nodeTree still apropriate or has to change
            int localIndex = (int)((nodeIndex) & ((1 << (nodeTree.getMaxLevel()-tau))-1));  //modulo: for to local index
            nodeTree.releaseRightByIndex(tau, localIndex);                      //release the node
        }
    }

    private void calcRightNodes(int tau){
        long levelIndex = leaveIndex;

        //Fetch/Clac rights up to tau
        for(SparseFractalSubTree activeTree: subTrees){
            //we reached tau?
            if(activeTree.getMinLevel() > tau) break; //break if this is the end
            //update high tree hash (calcs right nodes)
            if(!activeTree.hasStopped())activeTree.updateTreeHashHigh();

            //calc indexes
            int localIndex = (int)((levelIndex^1) & ((1 << (activeTree.getHeight()))-1));  //modulo: for to local index
            assert(levelIndex == (leaveIndex >>> activeTree.getMinLevel()));
            //adapt levelIndex for next run
            levelIndex =  levelIndex >>> activeTree.getHeight();

            //calc right auth nodes (go through all level of subtree but no higher then tau)
            for (int i= activeTree.getMinLevel() ;i < tau && i < activeTree.getMaxLevel();i++){
                //Fetch it
                auths[i] = new AuthEntry(activeTree.getRightByIndex(i, localIndex),false);     //this does not use additional spaceMax, because its still in subtrees
                localIndex = localIndex >>> 1;
            }

            measureDynamicSpace();      //measure
        }
    }

    //called by initializer
    public final void provide(DataArray value, int level, long levelIndex) {
        //we store only right nodes
        if ((levelIndex & 1) == 1) {
            //take second on each level as part of first auth path
            if(levelIndex == 1){                             //is second
                auths[level] =  new AuthEntry(value,false);  //setIt
                if(level == auths.length-1) return;          //we do never need this for something else then the first auth path //we would not even need the slot
            }

            //use incremental tree search to prevent looping all the time over and over again
            if(level == 0) leaveIndex = 0;                          //if its 0 start from scratch
            //try last one
            SparseFractalSubTree tree = subTrees[(int)leaveIndex];
            int maxLevel = tree.getMaxLevel();
            if(level >= maxLevel) {                                 //if last one isnt it any more, its the next one
                tree = subTrees[(int)++leaveIndex];
                maxLevel = tree.getMaxLevel();
            }
            //do the real init of the node if in range of the subtree
            if (levelIndex <= 1 << maxLevel - level)  tree.initNode(value, level - tree.getMinLevel(), (int) (levelIndex));
        }  else {
            //if its a left node may be the csprng have to be captured
            SparseFractalSubTree tree = subTrees[(int)leaveIndex];
            if(level >= tree.getMaxLevel()) {
                if(tree.cprng == null) tree.setCprng(initializer.cprng);
            }
        }

    }

    public final DataArray calcLeave(ICryptPrng localPrng){
        //calc leaf from prng
      return calcLeave(localPrng.current(),localPrng.index());
    }

    private DataArray  calcLeave(DataArray value, long index){
        //calc leaf from value (index just for loggin)
        if(MeasureToolbox.needsLeafData)MeasureToolbox.emitLeaveCalculation(index);   //measure
        return algs.getLeaveCalc().calcCommitmentLeave(value);
    }


    public final DataArray calcInnerNode(DataArray left, DataArray right) {
        //calc node from its childs
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();   //measure
        return algs.getHashFun().combineHashs(left, right);
    }

    @Override
    public final boolean canAuthMore() {
        long nLeave = updateDone?leaveIndex:leaveIndex+1;                       //depends on update or auth time
        //are we still in intit? Do we have more leaves?
        return initializer != null || nLeave < (1L << auths.length) ;           //+1 because leave index is pre increment sig
    }

    private void forceUpdate(){
        if(rootHash == null) finishInitialisation();  //ensure initialisation has finished
        if(!updateDone){                              //make sure update has run
            updateStep();
        }
        updateDone = true;
    }

    @Override
    public final ILeaveAuth createNextAuth() {
        forceUpdate();                              //ensure correct state
        IAuthEntry[] copy = auths.clone();          //copy array | Todo: is manual copy faster?
        assert(leaveIndex == cprng.index());
        DataArray Sk = cprng.current();             //get current Sk
        if((leaveIndex & 1) != 0)cprng.next() ;
        updateDone = false;
        //Create the result
        return new SimpleLeaveAuth(new SimpleAuthPath(copy),Sk);
    }
}
