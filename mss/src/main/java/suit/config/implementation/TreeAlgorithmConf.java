package suit.config.implementation;

import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.ILeafCalc;
import suit.config.interfaces.ITreeAlgorithmConf;


public class TreeAlgorithmConf implements ITreeAlgorithmConf {
    final IHashFunction h;
    final ILeafCalc l;

    public TreeAlgorithmConf(IHashFunction h, ILeafCalc l) {
        this.h = h;
        this.l = l;
    }

    @Override
    public IHashFunction getHashFun() {
        return h;
    }

    @Override
    public ILeafCalc getLeaveCalc() {
        return l;
    }

    @Override
    public String toString() {
        return "TreeAlgorithmConf{" +
                "hash=" + h +
                ", leaveCalc=" + l +
                '}';
    }
}
