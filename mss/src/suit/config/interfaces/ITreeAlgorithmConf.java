package suit.config.interfaces;

import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.ILeafCalc;

public interface ITreeAlgorithmConf {
    public IHashFunction getHashFun();
    public ILeafCalc getLeaveCalc();

}
