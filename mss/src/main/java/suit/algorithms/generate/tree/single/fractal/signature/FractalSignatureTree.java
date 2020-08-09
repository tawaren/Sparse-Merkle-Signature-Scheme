package suit.algorithms.generate.tree.single.fractal.signature;

import suit.algorithms.generate.tree.single.fractal.FractalTree;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IDetOneTimeSigGenerator;
import suit.algorithms.interfaces.ISignatureTree;
import suit.config.interfaces.ITreeSignatureAlgorithmConf;
import suit.interfaces.ITreeSig;
import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;

/**
 * Extension to use a Fractal Tree explicitly for signing
 * (Allows usage in multi Tree Enviroments like GMSS (Not yet implemented))
 */
public class FractalSignatureTree extends FractalTree implements ISignatureTree {

    //The algorithm for signature generation
    IDetOneTimeSigGenerator sigGen;

    public FractalSignatureTree(ITreeSignatureAlgorithmConf levelConf, int totalHeight,  ICryptPrng csprng) {
        super(levelConf, totalHeight, csprng);
        sigGen = levelConf.getOneTimeSignatureGenerator();
    }

    public FractalSignatureTree(ITreeSignatureAlgorithmConf levelConf, int levels, int totalHeight,  ICryptPrng csprng) {
        super(levelConf, levels, totalHeight, csprng);
        sigGen = levelConf.getOneTimeSignatureGenerator();
    }

    public FractalSignatureTree(ITreeSignatureAlgorithmConf levelConf, byte[] heights,  ICryptPrng csprng){
        super(levelConf, heights, csprng);
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
