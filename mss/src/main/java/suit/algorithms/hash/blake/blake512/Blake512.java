package suit.algorithms.hash.blake.blake512;


import suit.algorithms.hash.blake.BlakeLongCommon;
import suit.algorithms.hash.blake.BlakeMessageHasherConfig;
import suit.algorithms.interfaces.IFullHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;
import suit.tools.arrays.factory.LongDataFactory;

import java.util.Arrays;

/**
 * Implementation of the Blake512 algorithm (the Public Part)
 * parameters:
 *      - outLen is number of Bytes in output ( 0 < outLen <= 64 )
 *      - salt is a string for salting making the results of this HashFuntion different from others with no or other salt
 */public class Blake512 implements IFullHashFunction {

    final byte outLen;                          //Bytes outputted by hash
    final long[] salt;                          //Salt
    final IDataArrayFactory backingFactory;     //Factory for specific DataArray implementation

    //Constructor with no Salt
    public Blake512(byte outLen) {
        assert(outLen <= 64);
        this.outLen = outLen;
        salt = null;
        backingFactory = new LongDataFactory(DataArray.Endianess.BIG);
    }

    //Constructor with salt
    public Blake512(byte outLen, DataHolder salt) {
        assert(outLen <= 64);
        this.outLen = outLen;
        backingFactory = new LongDataFactory(DataArray.Endianess.BIG);
        this.salt = backingFactory.create(salt).asLongArray(); //do we need other endianess
    }

    //Constructor with default outLen(64)
    public Blake512() {
        outLen = 64;
        salt = null;
        backingFactory = new LongDataFactory(DataArray.Endianess.BIG);
    }

    //testonly
    public Blake512(IDataArrayFactory backingFactory) {
        outLen = 64;
        salt = null;
        this.backingFactory = backingFactory;
    }

    //Constructor with salt and default oultlen(64)
    public Blake512(DataHolder salt) {
        this.outLen = 64;
        backingFactory = new LongDataFactory(DataArray.Endianess.BIG);
        this.salt = backingFactory.create(salt).asLongArray(); //do we need other endianess
    }



    @Override
    public int getOutByteLen() {
        return outLen;
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        return backingFactory;
    }


    @Override
    //Creates a message Hasher to Hash large inputs
    public IMessageHasher createMessageHasher() {
        return new Blake512MessageHasher(BlakeMessageHasherConfig.standardConfig64);
    }

    @Override
    //Main function does a Hash of a DataArray (mostly hash of Hash)
    public DataArray hash(DataArray data) {

        //for performance: try direct.
        int dataLength = data.getByteSize();
        if(dataLength < 128 & (dataLength & 7) == 0){
            //We can do it in one compress step and without conversion (fastest path)
            //aligned input
            Blake512Instance inst = new Blake512Instance(salt);
            //is already zero padded (java does 0 memory before use)
            data.copyTo(inst.m, 0, dataLength >>> 3, DataArray.Endianess.BIG);  //do copy (no conversion happens if DataArray is long array)
           inst.countUp(dataLength);                                            //finish the first compress
           inst.prepareFinalPart(dataLength,null,0);                            //mark this as last compress
           inst.compress();                                                     //do the compress
           return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else if((dataLength & 7) == 0){
            //aligned input
            //We need multiple inputs, but no conversion  (medium fast path)
            Blake512Instance inst = new Blake512Instance(salt);
            long[] arr = data.asLongArray(DataArray.Endianess.BIG);      //convert to array
            int index = 0;          //index for next long
            int fill = 16;          //Bytes to compress in next run
            do{
                //copy up to fill longs into internal state
                for(int i = 0; i < fill & i < inst.m.length & i < arr.length-index; i++) inst.m[i] = arr[i+index];
                index += fill;                                      //advance index
                inst.countUp(fill << 3);                            //finish this compress
                fill = Math.min(16,arr.length - index);             //calc how many we have for next run
                if(fill == 0) inst.prepareFinalPart(index,null,0);  //if last mark as last
                inst.compress();                                    //do the compress
            }while(fill >= 16);
            //if not aligned input we have to pad
            if(fill != 0){
                //copy
                for(int i = 0; i < fill & i < inst.m.length & i < arr.length-index; i++) inst.m[i] = arr[i+index];
                //pad
                Arrays.fill(inst.m, fill, 16, 0); //do padding (their are data in upper part of m from last pass)
                inst.countUp(fill << 3);                        //finish this compress
                inst.prepareFinalPart(index + fill, null, 0);   //mark last
                inst.compress();                                //do the compress
            }
            return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else {
            //fallback to slow path (unaligned input)
            Blake512MessageHasher hasher = new Blake512MessageHasher(outLen,salt);   //create a full blown message hasher
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
        if(msg.getByteSize() != outLen && iterations != 0){
            msgH = hash(msg);
            iterations -= 1;
        } else{
            msgH = msg;
        }

        //are their itrations left
        if(iterations != 0){
            if((outLen & 7) == 0){
                Blake512Instance inst = new Blake512Instance(salt);
                inst.prepareFinalPart(outLen,null,0);                       //iterative hash is always last hash because one compress always suffice
                inst.countUp(outLen);                                       //their are always outLen Blocks hashed
                //no padding necessary because upper outLen Bytes stay zero
                int cpL = outLen >>> 3;
                msgH.copyTo(inst.m,0,cpL,DataArray.Endianess.BIG); //copy into internal state
                for(int i = 0; i < iterations-1;i++){
                    inst.compress();                                                            //do the compress
                    long[] h = inst.getHash();                                                  //fetch intermidiate result
                    for(int ii = 0; ii < cpL && ii < h.length && ii < inst.m.length;ii++) {     //use intermidiate as new input
                        inst.m[ii] = h[ii];
                    }
                    inst.initH(salt);                                                           //initialize internal state again
                }

                inst.compress();                                                                //do the last compress
                return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
            }  else {
                //Can we speedup some how?
                for(int i = 0; i < iterations-1;i++){
                    msgH = hash(msg);                                                           //do conventional hash
                }
                return msgH;
            }
        } else {
            return msg; //nothing more to do just return
        }
    }

    @Override
    //concates and hashces togehter two DataArrays (optimized for two hashes)
    public DataArray combineHashs(DataArray a, DataArray b) {
        int aLen = a.getByteSize();
        int bLen = b.getByteSize();

        //can it be done in one aligned compress
        if(aLen + bLen <= 128 && (aLen & 7) == 0 && (bLen & 7) == 0 ){
            //fast path for hashing 2 hashes
            int aLenL = aLen >>> 3;
            int bLenL = bLen >>> 3;

            Blake512Instance inst = new Blake512Instance(salt);
            //copy both to internal state
            a.copyTo(inst.m,0,aLenL, DataArray.Endianess.BIG);
            b.copyTo(inst.m,aLenL,bLenL, DataArray.Endianess.BIG);

            inst.countUp(aLen+bLen);                                //finish this compress
            inst.prepareFinalPart(aLenL+bLenL & 127, null, 0);      //flag last compress
            inst.compress();                                        //dot hte compress
            return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
        } else {
            return hash(backingFactory.concat(a,b));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Blake512 blake512 = (Blake512) o;

        if (outLen != blake512.outLen) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ((int) outLen) + 3000;
    }

    @Override
    public String toString() {
        return "Blake512{" +
                "outLen=" + outLen +
                '}';
    }
}