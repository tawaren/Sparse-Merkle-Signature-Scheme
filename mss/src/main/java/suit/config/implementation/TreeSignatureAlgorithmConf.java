package suit.config.implementation;

import suit.algorithms.interfaces.IDetOneTimeSigGenerator;
import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.ILeafCalc;
import suit.config.interfaces.ITreeSignatureAlgorithmConf;


public class TreeSignatureAlgorithmConf implements ITreeSignatureAlgorithmConf {
    final IHashFunction h;
    final IDetOneTimeSigGenerator s;

    public TreeSignatureAlgorithmConf(IHashFunction h, IDetOneTimeSigGenerator s) {
        this.h = h;
        this.s = s;
    }

    @Override
    public IHashFunction getHashFun() {
        return h;
    }

    @Override
    public IDetOneTimeSigGenerator getOneTimeSignatureGenerator() {
        return s;
    }

    @Override
    public ILeafCalc getLeaveCalc() {
        return s;
    }

    @Override
    public String toString() {
        return "TreeAlgorithmConf{" +
                "hash=" + h +
                ", sigGen=" + s +
                '}';
    }
}
