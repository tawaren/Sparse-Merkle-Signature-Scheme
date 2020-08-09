package suit.algorithms.generate.tree.single.advanced;

import suit.algorithms.generate.tree.AuthEntry;
import suit.algorithms.generate.tree.hash.TreeHash;
import suit.algorithms.generate.tree.interfaces.IFullCalcController;
import suit.algorithms.generate.tree.single.SimpleAuthPath;
import suit.algorithms.generate.tree.single.SimpleLeaveAuth;
import suit.algorithms.generate.tree.single.advanced.queue.ArrayRetainQueue;
import suit.algorithms.generate.tree.single.advanced.queue.RetainQueue;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IMerkleTree;
import suit.config.interfaces.ITreeAlgorithmConf;
import suit.interfaces.IAuthEntry;
import suit.interfaces.ILeaveAuth;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.MeasureToolbox;
import test.measure.SpaceMeasureable;

/**
 * LogSpaceTime Tree implementation [BDS08]
 * Algorithm with low memory Footprint
 * Parameter K allows for space time tradeoff (less efficent one then Fractal and Sparse Tree)
 * Does not work with StreamPrngs without other approach
 */
public class LogSpaceTimeTree implements IMerkleTree, IFullCalcController, SpaceMeasureable {

    @Override
    public void unMark() {
        for (SharedTreeHash t: treehashs) if(t != null) t.unMark();
        for (IAuthEntry a: auths) if(a != null) RefCountDataArray.prepare(a.getHash());
        for (DataArray k: keeps) if(k != null) RefCountDataArray.prepare(k);
        for (RetainQueue r: retains) if(r != null) r.unMark();
        if(initializer != null) initializer.unMark();
        if(cprng != null)cprng.unMark();

        //RefCountDataArray.prepare(rootHash);
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        for (SharedTreeHash t: treehashs) if(t != null) sum += t.markAndCount();
        for (IAuthEntry a: auths) if(a != null) sum += RefCountDataArray.count(a.getHash());
        for (DataArray k: keeps) if(k != null) sum += RefCountDataArray.count(k);
        for (RetainQueue r: retains) if(r != null) sum += r.markAndCount();
        if(initializer != null) sum += initializer.markAndCount();
        if(cprng != null)sum +=  cprng.markAndCount();

        //sum += RefCountDataArray.count(rootHash);
        return sum;
    }

    //helper method for space measuring
    private void measureDynamicSpace(){
        if(MeasureToolbox.needsDynamicSpaceData){
            unMark();
            int c = markAndCount();
            MeasureToolbox.emitDynamicSpaceMeasurement(c);
        }
    }

    private final IAuthEntry[] auths;               //current authentification path (is updated by update methode)
    private final SharedTreeHash[] treehashs;       //TreeHash instances with a shared Stack (calculates upcomming auth nodes on each level)
    private final RetainQueue[] retains;            //Queues for storing nodes near the top to increase performance (Size of Queues defined by K)
    private final DataArray[] keeps;                //Nodes needed for upcoming left node (other node is in auths)
    private DataArray rootHash;                     //The Pk
    protected final ITreeAlgorithmConf algs;        //the algorithms to use (hash etc...)
    private TreeHash initializer;                   //TreeHash for the initialisation and calcing the Pk
    private long leaveIndex = 0;                    //index of the current leaf (used twice once for init once for run)
    private final ICryptPrng cprng;                 //prng for calculating private leaf keys
    private boolean authReady = false;              //flag to cordinate update and next auth

    //Constructor
    public LogSpaceTimeTree(int h, int k, ITreeAlgorithmConf algs, ICryptPrng cprng) {
        assert(((h-k) & 1) == 0);                                           //Prereq of algorithm
        this.algs = algs;
        this.auths = new IAuthEntry[h];
        this.treehashs = new SharedTreeHash[h-k];                           //their is no TreeHash needed for the k top level
        DataArray[] sharedStack = new DataArray[h-k];                       //the shared stack (array version)

        for(int i = 0; i < treehashs.length; i++){
            treehashs[i] = new SharedTreeHash((byte)i,this,sharedStack);    //create the TreeHashes
        }

        //Create the Retain Queues (root node level does not need Queue, would be Pk if needed)
        this.retains = new RetainQueue[k-1];
        int size = 2;
        for(int i = retains.length-1; i >= 0; i--){
            retains[i] = new ArrayRetainQueue(size-1);      //-1 because the first value in the Queue can be used to finish the treeHashes directly during initialisation
            size <<= 1;                                     //each queue is half the size of the previous
        }
        this.keeps = new DataArray[auths.length-1];         //keeps will be needed in traverse phase but not init phase , if their will be a provide method, init it there
        this.cprng = cprng.capture();
        this.initializer = new TreeHash(this,cprng,h);
    }

    @Override
    public final boolean initStep() {
        assert(initializer != null);
        return initializer.step();              //do one initialisation step

    }

    @Override
    public final void init() {
        initializer.init();                     //do full initialisation
        finishInitialisation();
    }

    private void finishInitialisation(){
        assert(rootHash == null);       //should be called only once
        assert(initializer != null);
        rootHash = initializer.getRootHash();   //extract the Pk
        initializer = null;                     //free the initializer
        leaveIndex = 0;                         //initialize the index
        authReady = true;                       //after init first auth path is already ready
    }

    @Override
    //called by Initializer to allow hold on to calced nodes
    public final void provide(DataArray value, int level, long levelIndex){
        //catch prng? 3*2^h <-- leaf with index
        if(levelIndex == 1 && level < auths.length){
            auths[level] = new AuthEntry(value,false);              //if its the second node on a level and not the root store it in auth (second is first right node)
        }else if(levelIndex == 2 && level < treehashs.length){
            treehashs[level].setCprng(initializer.cprng);
        } else if(levelIndex == 3 && level < treehashs.length){
            treehashs[level].finishWith(value);                     //if its the fourth node on a level and not the root store it in treehashs (fourth is second right node)
        } else if(level >= treehashs.length && levelIndex >= 3 && ((levelIndex & 1) == 1)) {
            retains[level-treehashs.length].enqueue(value);         //if its in the top k level and not stored elsewhere then store it in the retain queues
        }
    }

    @Override
    public final DataArray getRootPk() {
        if(rootHash ==  null)finishInitialisation();
        return rootHash;
    }



    @Override
    public final DataArray calcLeave(ICryptPrng localPrng) {
        //calcs a leaf from an cprng
        if(MeasureToolbox.needsLeafData)MeasureToolbox.emitLeaveCalculation(leaveIndex);      //measure
        return algs.getLeaveCalc().calcCommitmentLeave(localPrng.current());
    }

    @Override
    public final DataArray calcInnerNode(DataArray left, DataArray right) {
        //calcs a node from its childs
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();                 //measure
        return algs.getHashFun().combineHashs(left,right);
    }

    @Override
    public final void updateStep() {
        //if the auth was not consumed do nothing
        if(!authReady && canAuthMore()){
            //updatePRNGs;
            for(SharedTreeHash sth: treehashs) sth.advanceNextPrng();
            //prepare next auths
            long levelLeave = leaveIndex++;                             //increase index
            //calc tau (first level on which auth path changes from right to left node) (or first bit in index where a 0 changes to a 1 on a +1)
            int tau = 0;
            //Devides unsigned by 2 or calc index on level tau (this breaks if index on tau is even) <- which means it is a left node
            for (;(levelLeave & 1) == 1;tau++) levelLeave >>>= 1;

            //(levelLeave & 2) shortcut for ((levelLeave >> 1) & 1) which is one level up and check for right or left node
            if((levelLeave & 2) == 0 && tau < keeps.length){   //is parent of leaveIndex node on level tau a left node ?
                keeps[tau] = auths[tau].getHash();             //we have too keep node for parent calculation
            }

            //get new auth nodes from treeHashes/authPath/keep
            computeNodes(tau);

            measureDynamicSpace();                 //measure

            //update the treeHashes for next updateStep
            updateTreeHashes();

            authReady = true;
            if(MeasureToolbox.needsRoundData)MeasureToolbox.finishRound(leaveIndex);                 //measure
        }
    }

    private void computeNodes(int tau){
        //calculate the left node
        if(tau == 0){
            //left node is leaf
            auths[0] =  new AuthEntry(calcLeave(cprng),true);
            cprng.next();
        } else {
            //left node is inner node
            //left child is previous auth path (is in auth because will be replaced later in this algorithm)
            //right child is stored in keep
            auths[tau] = new AuthEntry(calcInnerNode(auths[tau-1].getHash(),keeps[tau-1]),true);       //computation of the first left node  (this is where the auth path switches

            measureDynamicSpace();                  //measure

            keeps[tau-1] = null;                    //free keep no longer needed

            measureDynamicSpace();                  //measure

            //compute the new right nodes (all nodes with a smaller level than tau (remember tau is the first left node that changes so before it only rights changed and if a level change its child mus change to))
            for(int h = 0; h < tau; h++){
                if(h < treehashs.length){
                    //not in k top levels
                    //new Start index used for assert only, small jit should be able to eliminate if  asserts are off
                    long newStartIndex = leaveIndex + 3*(1 << h);                               //index of first leaf that have to be consumed to calc next right node for that treeHash
                    auths[h] = new AuthEntry(treehashs[h].getAndRestart(newStartIndex),false);  //get the calced node and restart the treehash with new newStartIndex
                } else{
                    //in k top levels (get from retain queue
                    auths[h] = new AuthEntry(retains[h - treehashs.length].dequeue(),false);    //get the node from the queue
                }
            }
        }
    }

    private void updateTreeHashes(){
        //Spend the budget of (H-K)/2 updates
        for (int budget = treehashs.length >> 1; budget > 0; budget--){
            int hLow = -1;                //level which needs update
            int low = Integer.MAX_VALUE;  //tailheight of the level which needs update
            for(int h = 0; h < treehashs.length; h++){
                int height = treehashs[h].getTailHeight();
                //does it have a new minimum tailheight & is not finished(if yes new update candidate found)
                if(low > height && treehashs[h].isActive()){
                    low = height;
                    hLow = h;
                }
            }

            if(hLow < 0) return;            //if we do not have a candidate break
            treehashs[hLow].update();       //update the candidate
            measureDynamicSpace();          //measure
        }
    }

    @Override
    public final boolean canAuthMore() {
        long nextSigLeave =  authReady?leaveIndex:leaveIndex+1;     //the index of the next leaf is dependent on authReady (because it may be already incremented or nor)
        return nextSigLeave < (1 << auths.length);                  //is the next leaf inside the bounds of the merklee tree
    }

    private void forceUpdate(){
        if(rootHash == null) finishInitialisation();    //make sure initialisation was finished proper (this throws if not)
        if(!authReady)updateStep();                     //update for signing if not done already
        authReady = true;
    }

    @Override
    public final ILeaveAuth createNextAuth() {
        forceUpdate();                                              //ensure update is done
        if(canAuthMore()){                                          //can we auth more?
            authReady = false;
            //copy the array to make sure no one can change auth and it stays the same to outside if we change it
            IAuthEntry[] copy = new IAuthEntry[auths.length];
            for(int i = 0; i < copy.length && i < auths.length;i++)copy[i] = auths[i];
            //Get Leaf Sk (auth authentificats Leaf PK corresponding to this Leaf SK)
            assert (leaveIndex == cprng.index());
            DataArray Sk = cprng.current();                                     //get the Leaf Sk
            if((leaveIndex & 1) != 0)cprng.next();
            //DataArray Sk = cprng.dataBlockForIndex(algs.getHashFun().getBackingFactory().create(leaveIndex));
            return new SimpleLeaveAuth(new SimpleAuthPath(copy),Sk);   //return the pair (is ready to be used as building block for other crypto like OTS)
        } else {
            //No more auths boom
            throw new IllegalArgumentException("out of Signatures"); // TODO: make own Exception
        }
    }

}
