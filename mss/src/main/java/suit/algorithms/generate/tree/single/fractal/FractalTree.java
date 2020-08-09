package suit.algorithms.generate.tree.single.fractal;


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
 * Fractal Tree implementation
 * Algorithm with good space-time trade off
 * Parameter L (levels) allows for space time trade off
 */
public class FractalTree implements IMerkleTree, IFullCalcController, SpaceMeasureable {


    @Override
    public void unMark() {
        if(initializer != null)initializer.unMark();
        //RefCountDataArray.prepare(rootHash);
        for (FractalSubTree t: subTrees) if(t != null) t.unMark();
        for (IAuthEntry a: activeAuths) RefCountDataArray.prepare(a.getHash());
        if(mainPrng != null)mainPrng.unMark();

    }

    @Override
    public int markAndCount() {
        int sum = 0;
        if(initializer != null)sum += initializer.markAndCount();
        //sum += RefCountDataArray.count(rootHash);
        for (FractalSubTree t: subTrees) if(t != null) sum += t.markAndCount();
        for (IAuthEntry a: activeAuths) sum += RefCountDataArray.count(a.getHash());
        if(mainPrng != null)sum +=  mainPrng.markAndCount();

        return sum;
    }

    public void measureDynamicSpace(){
        if(MeasureToolbox.needsDynamicSpaceData){
            unMark();
            int c = markAndCount();
            MeasureToolbox.emitDynamicSpaceMeasurement(c);
        }
    }

    protected int leaveIndex = 0;               //in initialisation is needed to trac the next leave for init, in auth mode is needed to trac the current leave

    public final ITreeAlgorithmConf algs;       //the algorithms to use (hash etc...)
    private final ICryptPrng mainPrng;          //prng for calculating private leaf keys
    private TreeHash initializer;               //TreeHash for the initialisation and calcing the Pk
    private DataArray rootHash = null;          //rootHash or initializer should be null never both
    private boolean authReady = true;           //flag to cordinate update and next auth
    protected final FractalSubTree[] subTrees;  //the subtrees which store the nodes
    private final IAuthEntry[] activeAuths;     //current authentification path (is updated by update methode)

    //Constructor with default level Calc
    public FractalTree(ITreeAlgorithmConf levelConf, int totalHeight,  ICryptPrng csprng) {
        this(levelConf, FractalTreeHeightCalc.calcOptHight(totalHeight), csprng);
    }

    //Constructor paramized by levels
    public FractalTree(ITreeAlgorithmConf levelConf, int levels, int totalHeight,  ICryptPrng csprng) {
        this(levelConf, FractalTreeHeightCalc.assignHeights(levels, totalHeight),csprng);
    }

    //Constructor witch is paramized by h of each subtree
    public FractalTree(ITreeAlgorithmConf levelConf, byte[] heights,  ICryptPrng cprng) {
        //calc height by sum up h's
        int tHeight = 0;
        for (byte height : heights) tHeight += height;
        this.algs = levelConf;
        this.initializer = new TreeHash(this,cprng.capture(), tHeight);
        //create empty structures
        subTrees = new FractalSubTree[heights.length];
        activeAuths = new IAuthEntry[tHeight];  //lowest is not stored
        this.mainPrng = cprng.capture();        //capture the prng to prevent influence fro outside


        byte minLevel = 0;
        //initialize the subtrees
        for (int i = 0; i < heights.length; i++){
            subTrees[i] = new FractalSubTree(minLevel,heights[i],this);  //its still open what with TopLevelTree & TreesWithoutDesire is happening
            minLevel += heights[i];
        }
    }

    @Override
    public final boolean initStep() {
        assert(initializer != null);
        return initializer.step();      //Do one initialisation step
    }

    public final void init() {
        assert(initializer != null);
        initializer.init();             //do full initialisation
        getRootPk();
    }

    protected final void finishInit(){
        //finish initialisation of all subtrees
        for (int i = 0; i < subTrees.length; i++) {
            subTrees[i].finishInitialisation(i < subTrees.length - 1);
        }
    }

    public final int getTotalLeaves(){
        return  1 << activeAuths.length;
    }

    @Override
    public final DataArray getRootPk() {
        if(rootHash == null){
            //should we do here initSteps if not done????
            rootHash = initializer.getRootHash();
            initializer = null;
            //the first leave is ready to be used for authentication
            finishInit();
            leaveIndex = 0;
            authReady = true;
        }
        assert(initializer == null);
        return rootHash;
    }

    @Override
    public final void updateStep() {
        //do nothing if the auth was not consumed
        if(!authReady){
            //update each subtree once
            for(FractalSubTree tree: subTrees)  tree.buildStep(leaveIndex);
            authReady = true;
            if(MeasureToolbox.needsRoundData)MeasureToolbox.finishRound(leaveIndex);          //measure
        }
    }

    @Override
    public final boolean canAuthMore() {
        return initializer != null || leaveIndex < (1 << activeAuths.length) ;   //we must be initialized to authenticate paths and their must be leaves left
    }

    public final DataArray calcLeave(ICryptPrng localPrng) {
        //calcs a leaf from an cprng
        if(MeasureToolbox.needsLeafData)MeasureToolbox.emitLeaveCalculation(leaveIndex);      //measure
        return algs.getLeaveCalc().calcCommitmentLeave(localPrng.current());
    }

    @Override
    public final DataArray calcInnerNode(DataArray left, DataArray right) {
        //calcs a node from its childs
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();                 //measure
        return algs.getHashFun().combineHashs(left, right);
    }

    @Override
    public final ILeaveAuth createNextAuth() {
        if(rootHash == null) getRootPk();                     //make sure RootIsCalced
        if(!authReady) updateStep();                          //make sure auth is ready
        if(!canAuthMore()) throw new IllegalStateException(); //check if we have more leaves | TODO: make own exception later

        //load auth portions from each subtree where a change occurd (subtree 0 always change, is handled seperately)
        subTrees[0].nextAuth(leaveIndex, activeAuths);
        for (int i = 1; i < subTrees.length;i++){
            FractalSubTree tree =  subTrees[i-1];
            if((leaveIndex & ((1 << tree.getMaxLevel()) -1)) == 0){       //check if something have change here
                subTrees[i].nextAuth(leaveIndex, activeAuths);
            } else {
                break;                                                    //Nothing changed, so higher wil not change either
            }
        }

        authReady = false;
        DataArray Sk = mainPrng.current();                                      //get the Leaf Sk
        leaveIndex++;                                                           //increase index
        mainPrng.next();                                                        //advance prng
        return new SimpleLeaveAuth(new SimpleAuthPath(activeAuths.clone()),Sk); //compose the result
    }

    @Override
    //called by Initializer to allow hold on to calced nodes
    public final void provide(DataArray value, int level, long levelIndex) {
        //find the subtree for this node and hand it over
        int minLevel = 0;
        for(FractalSubTree tree: subTrees){
            int levelsTooRoot = tree.getMaxLevel()-level;
            int nodesOnLevel = 1 << levelsTooRoot;

            //do we have to capture the curren prng (this is necessary if prng is at the pos of first needed node of the desired tree)
            if(tree.leafPrng == null && levelsTooRoot == 0){
                tree.setCprng(initializer.cprng);
            }

            //is the current subtree responsable for the node?
            if(levelIndex < nodesOnLevel && level >= minLevel && level < tree.getMaxLevel()){
                tree.initNextNode(value,level);
                break;
            }
            minLevel = tree.getMaxLevel();
        }
    }



}
