package standalone.mss.sparse;

import standalone.mss.sparse.auth.AuthEntry;
import standalone.mss.sparse.auth.AuthPath;
import standalone.mss.sparse.auth.LeaveAuth;
import standalone.mss.sparse.auth.TreeSig;
import standalone.prng.Prng;
import standalone.sig.OTSGen;
import standalone.utils.FractalTreeHeightCalc;

import static standalone.utils.TreeNavigationUtils.*;

/**
 * Sparse Tree implementation  (mix of Fractal and LogSpaceTime Tree)
 * Algorithm with good space-time trade off (memory footprint like LogSpaceTime Tree)
 * Parameter L (levels) allows for space time trade off
 */
public class SparseFractalTree {

    protected long leaveIndex = 0;                      //in initialisation is needed to track the next leave for init, in auth mode is needed to trac the current leave
    protected final SparseFractalSubTree[] subTrees;    //the subtrees which store the nodes
    private final AuthEntry[] auths;                    //current authentication path (is updated by update methode)
    protected final Prng cprng;                         //prng for calculating private leaf keys
    protected final TreeAlgorithmConf algs;             //the algorithms to use (hash etc...)

    protected TreeHash initializer;                     //TreeHash for the initialisation and computing the Pk, is null in auth mode
    private byte[] rootHash = null;                     //rootHash, is null during init

    private boolean updateDone = false;                 //flag to coordinate update and next auth
    private final OTSGen sigGen;                        //the ots signature algorithm used for generating the leafs


    //Constructor with default level Calculation
    public SparseFractalTree(TreeAlgorithmConf levelConf, int totalHeight, Prng csprng) {
        this(levelConf, FractalTreeHeightCalc.calcOptimalHeight(totalHeight), csprng);
    }

    //Constructor with number of levels and height fixed
    public SparseFractalTree(TreeAlgorithmConf levelConf, int levels, int totalHeight, Prng csprng) {
        this(levelConf, FractalTreeHeightCalc.assignHeights(levels, totalHeight),csprng);
    }

    //Constructor with individual h for each subtree
    public SparseFractalTree(TreeAlgorithmConf levelConf, byte[] heights, Prng csprng) {
        assert(csprng.index() == 0);    //ensure it is fresh
        this.cprng = csprng.capture();  //capture ensures that the passed in prng is not accidentally changed from the outside
        this.algs = levelConf;
        this.sigGen = levelConf.getLeafCalc();

        //calc height by summing up h's
        int totalHeight = 0;
        for (byte height : heights) totalHeight += height;
        //restricted because we work with longs
        // if a problem GMSS, CMSS or XMSS based schemes could help
        // or a switch to bigger numbers 128bit (not supported by Java)
        assert(totalHeight < 64);

        //initialize an empty authentication path
        this.auths = new AuthEntry[totalHeight];

        //create the initializer
        this.initializer = new TreeHash(this,csprng.capture(),auths.length);

        //Create the empty subtrees
        subTrees = new SparseFractalSubTree[heights.length];
        //prepare globally shared stacks
        byte[][] globalStackLow;
        byte[][] globalStackHigh;

        //are the global stacks needed or would they be empty
        if(heights.length > 1){
            //the heights of the top level subtrees
            int topmostH = heights[heights.length-1];
            int secondTopmostH = heights[heights.length-2];
            //the uppermost subtree never has nodes on this as it is prefilled on initialisation (it has no desired tree)
            globalStackHigh = new byte[totalHeight-topmostH][];
            //same as for higher, but lower does only compute the nodes node in the subtree (and as such  top 2 are not needed)
            globalStackLow = new byte[totalHeight-topmostH-secondTopmostH][];
        } else {
            globalStackHigh = new byte[0][];
            globalStackLow = new byte[0][];
        }

        //initialize the subtrees
        byte minLevel = 0;    //cumulative height
        for(int i = 0; i < subTrees.length;i++){
            byte h = heights[i]; //height of this
            //init with the stacks and heights
            subTrees[i] = new SparseFractalSubTree(minLevel,h, this, globalStackLow,globalStackHigh);
            minLevel += h;
        }
    }

    public final byte[] getRootPk() {
        if(rootHash == null) init();
        return rootHash;
    }

    public final int getHeight() {
        return auths.length;
    }

    public final int getNumTrees(){
        return subTrees.length;
    }

    public final void init() {
        assert(initializer != null);
        initializer.init();             //do full initialisation
        finishInitialisation();
    }

    private void finishInitialisation(){
        rootHash = initializer.getRootHash();               //get PK
        initializer = null;                                 //dealloc initializer
        subTrees[subTrees.length-1].stopCheck();            //stop top Subtree (is done)
        assert(subTrees[subTrees.length-1].hasStopped());   //ensure it was really stopped
        leaveIndex = 0;                                     //set the leaveIndex for auth path
        updateDone = true;                                  //a fresh tree is ready for the first signature
    }

    //schedules a single update of the lower tree hash
    private void doOneUpdateStep(){
        SparseFractalSubTree target = null;                                     //stores the tree which will receive the update
        //go over all trees until we find the one that should receive the update
        for (int i = 0; i < subTrees.length - 1; i++) {                         //top tree never receives an update as it is stopped (reason for -1)
            SparseFractalSubTree subTree = subTrees[i];
            //we ignore stopped trees (these have no mre desired trees to calculate)
            if (!subTree.hasStopped()) {
                if (target == null) {                                           //are we the first tree who can receive an update
                    target = subTree;                                           //we are the first not finished subtree, so we receive the update if no better candidate is found
                } else {
                    //if we have more than one active tree we choose the one with the lower tailHeight
                    if ((subTree.getTailHeight() < target.getTailHeight())) {   //are we a better candidate then the last one?
                        target = subTree;
                    }
                }
            }
        }
        //ensure we found something
        assert target != null;
        target.updateTreeHashLow();             //apply a update to the target
    }

    private void doBasicUpdate(){
        //distribute one update for every active subtree
        for (int i = 0; i < subTrees.length - 1; i++) {    //top tree never receives an update (reason for -1)
            if(!subTrees[i].hasStopped()) {
                doOneUpdateStep();                         //not it can be a completely different tree that receives the update
            }
        }
    }

    //makes an update step if needed and not already done
    public final void updateStep() {
        //do nothing if the auth was not consumed (or we can not auth more)
        if(!updateDone && canAuthMore()){
            leaveIndex++;                   //increase the index (go to the next leave used for signing)
            doBasicUpdate();                //do the updates
            calcNextAuthPath();             //calculate the next authentication path
            updateDone = true;              //mark it as done
        }
    }

    private void calcNextAuthPath(){
        //Find needed information for left node calc
        SparseFractalSubTree nodeTree = subTrees[0];
        long nodeIndex;
        //calc tau (first level on which auth path changes from containing a right node to containing a left node)
        // (or first bit in index where a 0 changes to a 1 on a +1) -- which is the same
        int tau = 0;
        //we already loaded 0, so next is 1
        int treeIndex = 1; //we track the tree separately as the loop already tracks the node index for that thee level
        //we start with the current, then each iter we divide by to to get index on next level (and we go a level up)
        //we stop when we find the first rightNode (he will change to a left as all under it are lefts)
        //  (0111..111) +1 => (1000..000)
        for (nodeIndex = leaveIndex; isLeftNode(nodeIndex); nodeIndex = parentLevelIndex(nodeIndex)){
            //track tau and the subtree in which tau is in
            if(tau++ >= nodeTree.getMaxLevel()) nodeTree = subTrees[treeIndex++];
        }
        //ensure we had not made a mistake
        assert (nodeIndex == ancestorLevelIndex(leaveIndex, tau));

        calcLeftNode(tau, nodeIndex, nodeTree);             //calc the new left auth node (node that switched from right to left)
        releaseRight(tau, treeIndex, nodeIndex, nodeTree);  //release no longer needed right node (because it left the Auth path and will never be needed again)
        calcRightNodes(tau);                                //calc the new right auth nodes (replacing the lefts under the new right)
    }

    private void calcLeftNode(int tau, long nodeIndex, SparseFractalSubTree nodeTree){
        //calc left auths
        if(tau == 0){ //if tau is zero then left node is a leaf
            //compute the new leaf
            byte[] newLeaf = calcLeaf(cprng);
            //advance the prng from which leaves are derived
            cprng.next();
            //create the Auth entry for the leaf
            auths[tau] = new AuthEntry(newLeaf,true);
        } else{ //left node is a inner node
            // we go up the tree by dividing the index of the leave by 2 until we arrive at the tau-1 level
            // we do this tau -1 times and then end up with the index of the right child of the node on level tau in the new auth path
            long childIndexRight = ancestorLevelIndex(leaveIndex,tau -1);
            long childIndexLeft = (childIndexRight-1);          //we jump over to the left Child
            int localIndex = (int)shiftIndexIntoTree(childIndexLeft,tau-1, nodeTree.getMaxLevel()) ;
            //Finally load the right node from its subtree
            byte[] right = nodeTree.getRightByIndex(tau-1,localIndex);   //fetch the right node
            //releaseRightNodes no longer needed for leftCalc (this is whats in keep in orig algo)
            nodeTree.releaseRightByIndex(tau-1, localIndex);
            //fetch the left child node (current auth)
            byte[] left = auths[tau-1].getHash();
            //compute the new left node
            byte[] newNode= calcInnerNode(left,right);
            //put it on the auth path
            auths[tau] = new AuthEntry(newNode,true);
        }

    }

    private void releaseRight(int tau, int treeIndex, long nodeIndex, SparseFractalSubTree nodeTree){
        //release right node (sibling of the newly computed left node) if its no longer needed for AuthPath calc
        if(isRightNode(parentLevelIndex(nodeIndex))){                                 //if parent is right node we do not need it anymore (else its still needed for left calc)
            if(tau >= nodeTree.getMaxLevel()) nodeTree = subTrees[treeIndex];         //is nodeTree still the right one or has it changed
            //to get the local index
            int localIndex = (int)shiftIndexIntoTree(nodeIndex,tau,nodeTree.getMaxLevel());
             //release the node
            nodeTree.releaseRightByIndex(tau, localIndex);
        }
    }

    private void calcRightNodes(int tau){
        long levelIndex = leaveIndex;

        //Fetch/Compute rights up to tau -- we need to touch the subtrees for that
        for(SparseFractalSubTree activeTree: subTrees){
            //if we reached tau we are done
            if(activeTree.getMinLevel() > tau) break; //break if this is the end
            //update high tree hash (Compute right nodes)
            if(!activeTree.hasStopped()) {
                //this makes sure the node we need is ready
                activeTree.updateTreeHashHigh();
            }

            //calc indexes (local to active subtree)
            long siblingIndex = levelIndex^1;  // first we need the sibling nt the one the path
            //to get the local index
            int localIndex =(int)shiftIndexIntoTree(siblingIndex,0,activeTree.getHeight()); //note is already relative to activeTree leaf level so 0 as level is io
            assert(levelIndex == ancestorLevelIndex(leaveIndex,activeTree.getMinLevel())); //ensure that next step was done right up to now
            //adapt levelIndex for next run -- we jump right to the next trees leaveLevel
            levelIndex = ancestorLevelIndex(levelIndex, activeTree.getHeight()); //note is already relative to activeTree leaf level so 0 as level is io

            //calc right auth nodes (go through all level of subtree but no higher then tau)
            for (int i= activeTree.getMinLevel() ;i < tau && i < activeTree.getMaxLevel();i++){
                //Fetch it
                byte[] newNode = activeTree.getRightByIndex(i, localIndex);
                //add it to the auth path
                auths[i] = new AuthEntry(newNode,false);     //this does not use additional spaceMax, because its still in subtrees
                //shift the local index one level up
                localIndex = (int)parentLevelIndex(localIndex);
            }
        }
    }

    //called by initializer when a new node is computed
    //this allows us to build the initial state
    public final void provide(byte[] value, int level, long levelIndex) {
        //we store only right nodes
        if (isRightNode(levelIndex)) {
            //take second on each level as part of first auth path
            if(levelIndex == 1){                                        //is second
                auths[level] = new AuthEntry(value,false);    //setIt
                //the top node is not needed for anything else
                if(level == auths.length-1) return;
            }

            //use incremental tree search to prevent looping all the time over and over again
            //we missuse leafveIndex here but it is treeIndex (but ths spares space)
            if(level == 0) leaveIndex = 0;                          //if its 0 start from scratch (escape hatch in the incremental search)
            //try last one
            SparseFractalSubTree tree = subTrees[(int)leaveIndex];
            int maxLevel = tree.getMaxLevel();
            //if last one is not correct any more, go to the next one
            if(level >= maxLevel) {
                tree = subTrees[(int)++leaveIndex];
                maxLevel = tree.getMaxLevel();
            }
            //do the real init of the node if in range of the subtree
            //if it is a node in the first exist tree store it
            if (levelIndex <= leavesInTreeWithHeight(maxLevel - level))  {
                //level in relation to subtree
                int localLevel = level - tree.getMinLevel();
                //make the actual init (delegated to subtree)
                tree.initNode(value, localLevel, (int) (levelIndex));
            }
        }  else {
            //if its a left node may be the csprng has to be captured
            SparseFractalSubTree tree = subTrees[(int)leaveIndex];
            //if we are in the current subtree
            if(level >= tree.getMaxLevel()) {
                //set the prng if not yet done
                if(tree.cprng == null) tree.setCprng(initializer.cprng);
            }
        }

    }

    public final byte[] calcLeaf(Prng localPrng){
        //calc sk from prng
        byte[] sk = localPrng.current();
        //calc leaf from sk (is pk of winternitz with that sk)
        return algs.getLeafCalc().calcCommitmentLeave(sk);
    }

    public final byte[] calcInnerNode(byte[] left, byte[] right) {
        //calc node from its childs
        return algs.getHashFun().combineHashes(left, right);
    }

    public final boolean canAuthMore() {
        long nLeave = updateDone?leaveIndex:leaveIndex+1;                       //depends on update or auth time
        //are we still in init? Do we have more leaves?
        return initializer != null || nLeave < leavesInTreeWithHeight(auths.length) ;
    }

    private void forceUpdate(){
        //if rootHash not yet computed do it
        if(rootHash == null) finishInitialisation();  //ensure initialisation has finished
        //if update not yet done do it
        if(!updateDone) updateStep();
        //Mark as done
        updateDone = true;
    }

    public final LeaveAuth createNextAuth() {
        //first ensure the update was done
        forceUpdate();
        //ensure that the indexes are still aligned
        assert(leaveIndex == cprng.index());
        //get current Sk
        byte[] Sk = cprng.current();
        //if right node advance the cprng (who does it for left???)
        if(isRightNode(leaveIndex))cprng.next() ;
        //mark that we need a new update before we can continue
        updateDone = false;
        //Create the result (returns a copy of the auth path so it does not change)
        return new LeaveAuth(new AuthPath(auths.clone()),Sk);
    }

    //sign a message
    public TreeSig createSignature(byte[] msg) {
        //sign with current and pack together wih auth path
        return sigGen.calcSignature(createNextAuth(),msg);
    }
}
