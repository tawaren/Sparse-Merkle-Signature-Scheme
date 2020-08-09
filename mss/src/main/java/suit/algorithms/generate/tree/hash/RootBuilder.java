package suit.algorithms.generate.tree.hash;


import suit.algorithms.generate.tree.EmptyTraverser;
import suit.algorithms.generate.tree.interfaces.INodeCalcController;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.ITreeRootCalc;
import suit.config.interfaces.ITreeAlgorithmConf;
import suit.tools.arrays.DataArray;
import test.measure.SpaceMeasureable;

/**
 * Helper to calc the Pk independent of a traversing implementation
 */
public class RootBuilder implements INodeCalcController, ITreeRootCalc, SpaceMeasureable {

    //the used Algorithms
    protected final ITreeAlgorithmConf algs;
    //the used TreeHash
    private final TreeHash th;

    @Override
    public void unMark() {
        th.unMark();
    }

    @Override
    public int markAndCount() {
        return th.markAndCount();
    }

    //The Constructor, needs the algos the leaf generator prng and the height
    public RootBuilder(ITreeAlgorithmConf levelConf, int height, ICryptPrng csprng){
        this.algs = levelConf;
        this.th = new TreeHash(new EmptyTraverser(this), csprng.capture(), height);

    }

    public DataArray calcLeave(ICryptPrng localPrng){
        //calc leaf with next sk
        return algs.getLeaveCalc().calcCommitmentLeave(localPrng.current());
    }


    public DataArray calcInnerNode(DataArray left, DataArray right) {
        //calc inner node by hashing children together
        return algs.getHashFun().combineHashs(left, right);
    }

    @Override
    public DataArray calcRootHash() {
        th.init();
        //just forward
        return th.getRootHash();
    }
}
