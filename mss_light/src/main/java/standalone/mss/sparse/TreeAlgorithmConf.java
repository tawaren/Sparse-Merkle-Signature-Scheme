package standalone.mss.sparse;

import standalone.hash.HashFunction;
import standalone.sig.OTSGen;


public class TreeAlgorithmConf {
    final HashFunction h;
    final OTSGen l;

    public TreeAlgorithmConf(HashFunction h, OTSGen l) {
        this.h = h;
        this.l = l;
    }

    public HashFunction getHashFun() {
        return h;
    }

    public OTSGen getLeafCalc() {
        return l;
    }

}
