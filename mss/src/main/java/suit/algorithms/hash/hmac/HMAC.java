package suit.algorithms.hash.hmac;

import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

import java.util.Arrays;

/**
 * Creates an KeyedHash Function from an unkeyed one
 * uses HMAC algorithm
 */
public class HMAC implements IKeyedHashFunction {

    private final DataArray opad;               //outer const outlen bytes
    private static final byte opart = 92;       //outer const single byte
    private final DataArray ipad;               //inner const outlen bytes
    private static final byte ipart = 54;       //inner const single byte
    private final IHashFunction hash;           //inner hash function

    //Constructor
    public HMAC(IHashFunction hash) {
        this.hash = hash;
        int len = hash.getOutByteLen();
        byte[] pad = new byte[len];
        Arrays.fill(pad, opart);
        opad = hash.getBackingFactory().create(pad);
        pad = new byte[len];
        Arrays.fill(pad, ipart);
        ipad = hash.getBackingFactory().create(pad);
    }

    @Override
    public int getOutByteLen() {
        return hash.getOutByteLen();
    }

    @Override
    public IDataArrayFactory getBackingFactory(){
        return hash.getBackingFactory();
    }

    @Override
    //the keyed hash function
    public DataArray mac(DataArray Key, DataArray data) {
        //adapt length if necessary
        if(data.getByteSize() != hash.getOutByteLen()){
            Key = hash.hash(Key);   // TODO: in < case shortening would be enought
        }
        return hash.combineHashs(Key.xor(opad),hash.combineHashs(Key.xor(ipad), data));
    }
}
