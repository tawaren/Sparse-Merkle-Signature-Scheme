package suit.tools;


import suit.algorithms.hash.hmac.HMAC;
import suit.algorithms.interfaces.IFullHashFunction;
import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

import java.security.MessageDigest;

/**
 * Wrapper to use Java Cryptographics with this Framework
 */
public class MessageDigestHash implements IFullHashFunction {
    //the hash function this is based on
    IHashFunction digest;

    //a HMAC instance to provide keyed hash functionality
    IKeyedHashFunction hmac;

    public MessageDigestHash(MessageDigest digest) {
        this(digest, null, null);
    }

    public MessageDigestHash(MessageDigest digest ,DataHolder salt) {
        this(digest, salt, null);
    }

    public MessageDigestHash(MessageDigest digest, byte outLen) {
        this(digest, null, outLen);
    }

    public MessageDigestHash(MessageDigest digest ,DataHolder salt, Byte outLen) {
        this.digest = new MessageDigestWrapper(digest ,salt, outLen);
        this.hmac = new HMAC(this.digest);
    }

    @Override
    public IMessageHasher createMessageHasher() {
        return digest.createMessageHasher();
    }

    @Override
    public int getOutByteLen() {
        return digest.getOutByteLen();
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        return digest.getBackingFactory();
    }

    @Override
    public DataArray hash(DataArray data) {
        return digest.hash(data);
    }

    @Override
    public DataArray iterativeHash(DataArray msg, int iterations) {
        return digest.iterativeHash(msg,iterations);
    }

    @Override
    public DataArray combineHashs(DataArray a, DataArray b) {
        return digest.combineHashs(a,b);
    }

    @Override
    public DataArray mac(DataArray Key, DataArray data) {
        return hmac.mac(Key,data);
    }
}
