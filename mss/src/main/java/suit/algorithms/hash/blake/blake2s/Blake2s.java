package suit.algorithms.hash.blake.blake2s;

import suit.algorithms.hash.blake.BlakeIntCommon;
import suit.algorithms.hash.blake.BlakeMessageHasherConfig;
import suit.algorithms.interfaces.IFullHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;
import suit.tools.arrays.factory.IntDataFactory;

import java.util.Arrays;

/**
 * Implementation of the Blake2s algorithm (the Public Part)
 * parameters:
 *      - outLen is number of Bytes in output ( 0 < outLen <= 32 )
 *      - salt is a string for salting making the results of this HashFuntion different from others with no or other salt
 */public final class Blake2s implements IFullHashFunction {

    final byte outLen;                          //Bytes outputted by hash
    final int[] salt;                           //Salt
    final IDataArrayFactory backingFactory;     //Factory for specific DataArray implementation

    //Constructor with no Salt
    public Blake2s(byte outLen) {
        assert(outLen <= 32);
        this.outLen = outLen;
        salt = null;
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
    }

    //Constructor with salt
    public Blake2s(byte outLen, DataHolder salt) {
        assert(outLen <= 32);
        this.outLen = outLen;
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.salt = backingFactory.create(salt).asIntArray(); //do we need other endianess
    }

    //Constructor with default outLen(64)
    public Blake2s() {
        outLen = 32;
        salt = null;
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
    }

    //Constructor with salt and default oultlen(64)
    public Blake2s(DataHolder salt) {
        this.outLen = 32;
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.salt = backingFactory.create(salt).asIntArray(); //do we need other endianess
    }

    @Override
    public int getOutByteLen() {
        return outLen;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        return backingFactory;
    }

    @Override
    //Creates a message Hasher to Hash large inputs
    public IMessageHasher createMessageHasher() {
        return new Blake2sMessageHasher(BlakeMessageHasherConfig.standardConfig32);
    }

    @Override
    //Main function does a Hash of a DataArray (mostly hash of Hash)
    public DataArray hash(DataArray data) {

        //for performance check, try update fallback always (should be slower, because of the conversions going on)
        int dataLength = data.getByteSize();
        if(dataLength <= 64 & (dataLength & 3) == 0){
           //We can do it in one compress step and without conversion (fastest path)
           //aligned input
           Blake2sInstance inst = new Blake2sInstance(outLen,salt);
            //is already zero padded (java does 0 memory before use)
           data.copyTo(inst.m, 0, dataLength >>> 2, DataArray.Endianess.LITTLE);
           inst.countUp(dataLength);        //finish the first compress
           inst.flagFinal();                //mark this as last compress
           inst.compress();                 //do the compress
           return BlakeIntCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else if((dataLength & 3) == 0){
            //aligned input
            //We need multiple inputs, but no conversion  (medium fast path)
            Blake2sInstance inst = new Blake2sInstance(outLen,salt);
            int[] arr = data.asIntArray(DataArray.Endianess.LITTLE);
            int index = 0;          //index for next long
            int fill = 16;          //Bytes to compress in next run
            do{
                //copy up to fill longs into internal state
                for(int i = 0; i < fill & i < inst.m.length & i < arr.length-index; i++) inst.m[i] = arr[i+index];
                index += fill;                              //advance index
                inst.countUp(fill << 2);                    //finish this compress
                fill = Math.min(16,arr.length - index);     //calc how many we have for next run
                if(fill == 0) inst.flagFinal();             //if last mark as last
                inst.compress();                            //do the compress
            }while(fill >= 16);

            //if not aligned input we have to pad
            if(fill != 0){
                //copy
                for(int i = 0; i < fill & i < inst.m.length & i < arr.length-index; i++) inst.m[i] = arr[i+index];
                //pad
                Arrays.fill(inst.m, fill, 16, 0); //do padding (their are data in upper part of m from last pass)
                inst.countUp(fill << 2);                    //finish this compress
                inst.flagFinal();                           //mark last
                inst.compress();                            //do the compress
            }
            return BlakeIntCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else {
            //fallback to slow path (unaligned input)
            Blake2sMessageHasher hasher = new Blake2sMessageHasher(outLen,salt);     //create a full blown message hasher
            hasher.update(data.asByteArray());                                       //hash the message
            return hasher.finalStep();                                               //create the result
        }

    }

    @Override
    //hash with key
    public DataArray mac(DataArray Key, DataArray data) {
        return combineHashs(Key,data);
    }

    @Override
    //hash iteration times (skiping state conversions between hashes)
    public DataArray iterativeHash(DataArray msg, int iterations) {
        DataArray msgH;
        //if the input is not aligned start with conventional hash
        if(msg.getByteSize() != outLen){
            msgH = hash(msg);
            iterations -= 1;
        } else{
            msgH = msg;
        }

        if(iterations != 0){
            if((outLen & 3) == 0){
                Blake2sInstance inst = new Blake2sInstance(outLen,salt);
                inst.flagFinal();                                           //iterative hash is always last hash because one compress always suffice
                inst.countUp(outLen);                                       //their are always outLen Blocks hashed
                //no padding necessary because upper outLen Bytes stay zero
                int cpL = outLen >>> 2;
                msgH.copyTo(inst.m,0,cpL, DataArray.Endianess.LITTLE);      //copy into internal state
                for(int i = 0; i < iterations-1;i++){
                    inst.compress();                                                            //do the compress
                    int[] h = inst.getHash();                                                  //fetch intermidiate result
                    for(int ii = 0; ii < cpL && ii < h.length && ii < inst.m.length;ii++) {     //use intermidiate as new input
                        inst.m[ii] = h[ii];
                    }
                    inst.initH(outLen,salt);                                                    //initialize internal state again
                }
                inst.compress();                                            //do the last compress
                return BlakeIntCommon.genResult(inst.getHash(), outLen, backingFactory);
            } else {
                //Can we speedup some how?
                for(int i = 0; i < iterations-1;i++){
                    msgH = hash(msg);                                        //do conventional hash
                }
                return msgH;
            }
        } else {
            return msg;                                                     //nothing more to do just return
        }
    }

    @Override
    //concates and hashces togehter two DataArrays (optimized for two hashes)
    public DataArray combineHashs(DataArray a, DataArray b) {
        int aLen = a.getByteSize();
        int bLen = b.getByteSize();

        //can it be done in one aligned compress
        if(aLen + bLen <= 64 && (aLen & 3) == 0 && (bLen & 3) == 0){
            //fast path for hashing 2 hashes
            Blake2sInstance inst = new Blake2sInstance(outLen,salt);
            //copy both to internal state
            a.copyTo(inst.m,0,aLen >>> 2, DataArray.Endianess.LITTLE);
            b.copyTo(inst.m,aLen >>> 2, bLen >>> 2, DataArray.Endianess.LITTLE);
            inst.flagFinal();                   //flag last compress
            inst.countUp(aLen+bLen);            //finish this compress
            inst.compress();                    //dot hte compress
            return BlakeIntCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else {
            return hash(backingFactory.concat(a,b));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blake2s blake2s = (Blake2s) o;

        if (outLen != blake2s.outLen) return false;
        if (!backingFactory.equals(blake2s.backingFactory)) return false;
        if (!Arrays.equals(salt, blake2s.salt)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) outLen;
        result = 31 * result + (salt != null ? Arrays.hashCode(salt) : 0);
        result = 31 * result + backingFactory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Blake2s{" +
                "outLen=" + outLen +
                ", salt=" + backingFactory.create(salt).toString() +
                '}';
    }
}