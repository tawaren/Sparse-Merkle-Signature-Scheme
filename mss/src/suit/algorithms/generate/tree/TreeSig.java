package suit.algorithms.generate.tree;


import suit.interfaces.IAuthPath;
import suit.interfaces.ITreeSig;
import suit.tools.arrays.DataArray;

import java.util.Arrays;

/**
 * Complete Signature
 */
public final class TreeSig implements ITreeSig {
    //Signature
    DataArray[] sig;
    //Signature Auth
    IAuthPath  authPath;

    public TreeSig(DataArray[] sig, IAuthPath authPath) {
        this.sig = sig;
        this.authPath = authPath;
    }

    @Override
    public DataArray[] getSig() {
        return sig;
    }

    @Override
    public IAuthPath getAuthPath() {
        return authPath;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeSig sig1 = (TreeSig) o;

        return authPath.equals(sig1.authPath) && Arrays.equals(sig, sig1.sig);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sig);
        result = 31 * result + authPath.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Sig{" +
                "rootHashSig=" + Arrays.toString(sig) +
                ", authPath=" + authPath +
                '}';
    }
}
