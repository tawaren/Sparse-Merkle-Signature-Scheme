package standalone.mss.sparse.auth;

import java.util.Arrays;

/**
 * Complete Signature
 */
public final class TreeSig {
    //Signature
    byte[][] sig;
    //Signature Auth
    AuthPath authPath;

    public TreeSig(byte[][] sig, AuthPath authPath) {
        this.sig = sig;
        this.authPath = authPath;
    }

    public byte[][] getSig() {
        return sig;
    }

    public AuthPath getAuthPath() {
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
}
