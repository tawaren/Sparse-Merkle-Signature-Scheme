package suit.algorithms.hash.blake.blake2s;

import suit.algorithms.hash.blake.BlakeIntCommon;
import suit.algorithms.hash.blake.BlakeMessageHasher;
import suit.algorithms.hash.blake.BlakeMessageHasherConfig;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;
import suit.tools.arrays.factory.IntDataFactory;

import java.util.Arrays;

/**
 * Input manager for large Messages hashing with Blake2s
 */
public final class Blake2sMessageHasher extends BlakeMessageHasher {
    private final Blake2sInstance inst;                 //Underlying instance
    private final IDataArrayFactory backingFactory;     //Factory for specific DataArray implementation

   //Condstructor from Config
    public Blake2sMessageHasher(final BlakeMessageHasherConfig conf) {
        super(conf);
        DataArray salt = conf.getSalt();
        int[] iSalt = null;
        if(salt != null) iSalt = salt.asIntArray();
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.inst = new Blake2sInstance(outLen,iSalt);
    }

    //Condstructor
    protected Blake2sMessageHasher(byte outLen, int[] salt) {
        super(outLen);
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.inst =  new Blake2sInstance(outLen, salt);
    }


    @Override
    protected final void countUp(final int n) {
        inst.countUp(n);
    }

    @Override
    protected final void compress() {
        inst.compress();
    }

    @Override
    protected final void putBytes(int i, byte[] data, int dataIndex) {
        inst.m[i >>> 2] = BlakeIntCommon.getInt(data, dataIndex);
    }

    @Override
    protected final int getAlign() {
        return 4;
    }

    //Produce the Final value
    public final DataArray finalStep()
    {
        inst.countUp(bufIndex);
        final int open = bufIndex%4;                        //whats Open
        if(open != 0){

            //byte padding
            Arrays.fill(notEven, open, 4, (byte) 0);        //pad
            putBytes(bufIndex,notEven,0);                   //put padded rest
            bufIndex += 4-open;
        }

        if(bufIndex != 0 && bufIndex != 64){
            //long Padding
            Arrays.fill(inst.m, bufIndex >>> 2, 16, 0);
        }

        inst.flagFinal();      //Mark last compress
        inst.compress();       //Do Last Compress

        return BlakeIntCommon.genResult(inst.getHash(), outLen, backingFactory);
    }

}
