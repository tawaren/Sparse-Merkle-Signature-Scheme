package standalone.hash;


import standalone.utils.DataUtils;

import java.util.Arrays;

/**
 * Creates an KeyedHash Function from an unkeyed one
 * uses HMAC algorithm
 */
public class HMAC implements FullHashFunction {

    private final byte[] opad;                  //outer const outlen bytes
    private static final byte opart = 92;       //outer const single byte
    private final byte[] ipad;                  //inner const outlen bytes
    private static final byte ipart = 54;       //inner const single byte
    private final HashFunction hash;            //inner hash function

    //Constructor
    public HMAC(HashFunction hash) {
        this.hash = hash;
        int len = hash.getOutByteLen();
        byte[] pad = new byte[len];
        Arrays.fill(pad, opart);
        opad = pad;
        pad = new byte[len];
        Arrays.fill(pad, ipart);
        ipad = pad;
    }

    @Override
    public int getOutByteLen() {
        return hash.getOutByteLen();
    }

    //the keyed hash function
    public byte[] mac(byte[] Key, byte[] data) {
        //adapt length if necessary
        if(data.length != hash.getOutByteLen()){
            Key = hash.hash(Key);   // TODO: in < case shortening would be enought
        }
        return hash.combineHashes(DataUtils.xor(Key,opad),hash.combineHashes(DataUtils.xor(Key,ipad), data));
    }

    @Override
    public byte[] hash(byte[] data) {
        return hash.hash(data);
    }

    @Override
    public byte[] iterativeHash(byte[] msg, int iterations) {
        return hash.iterativeHash(msg,iterations);
    }

    @Override
    public byte[] combineHashes(byte[] a, byte[] b) {
        return hash.combineHashes(a,b);
    }
}
