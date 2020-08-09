package suit.algorithms.generate.tree.single.advanced.signature;

import suit.algorithms.generate.tree.single.advanced.LogSpaceTimeTree;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IDetOneTimeSigGenerator;
import suit.algorithms.interfaces.ISignatureTree;
import suit.config.interfaces.ITreeSignatureAlgorithmConf;
import suit.interfaces.ITreeSig;
import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;

/**
 * Extension to use a LogSpaceTime Tree explicitly for signing
 * (Allows usage in multi Tree Enviroments like GMSS (Not yet implemented))
 */
public final class LogSpaceTimeSignatureTree extends LogSpaceTimeTree implements ISignatureTree {

    //The algorithm for signature generation
    final IDetOneTimeSigGenerator sigGen;

    public LogSpaceTimeSignatureTree(int h, int k, ITreeSignatureAlgorithmConf levelConf, ICryptPrng csprng) {
        super(h,k,levelConf, csprng);
        sigGen = levelConf.getOneTimeSignatureGenerator();
    }

    @Override
    //sign a message
    public ITreeSig createSignature(DataArray msg) {
        return sigGen.calcSignature(createNextAuth(),msg);
    }

    @Override
    //sign a message from data not yet as DataArray
    public ITreeSig createSignature(DataHolder message) {
        return sigGen.calcSignature(createNextAuth(), sigGen.getBackingFactory().create(message));
    }

}
