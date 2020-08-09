package standalone.mss.sparse;

import org.junit.jupiter.api.Test;
import standalone.hash.HMAC;
import standalone.hash.HashFunction;
import standalone.hash.KeyedHashFunction;
import standalone.hash.MessageDigestWrapper;
import standalone.prng.Hash_DBRG;
import standalone.sig.WinternitzSigGen;
import standalone.sig.WinternitzSigVerify;
import standalone.utils.DataUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class SparseFractalTreeTest {

    @Test
    public void testSparseMss() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        var h = new MessageDigestWrapper(digest);
        var kh = new HMAC(h);
        var l = new WinternitzSigGen((byte)4,kh,h);
        var v = new WinternitzSigVerify((byte)4,kh,h);
        var sk = DataUtils.random(h.getOutByteLen());
        var prng = new Hash_DBRG(h,sk);
        var conf = new TreeAlgorithmConf(h,l);
        var tree = new SparseFractalTree(conf, 16, prng);
        tree.init();
        var treePk = tree.getRootPk();
        while (tree.canAuthMore()){
            var msg = DataUtils.random(128);
            var sig = tree.createSignature(msg);
            var leafPk = v.verifySignature(msg, sig.getSig());
            var root = sig.getAuthPath().computeRoot(h, leafPk);
            assertArrayEquals(treePk,root);
        }
    }

}