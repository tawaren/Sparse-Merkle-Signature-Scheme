package suit.algorithms.hash.blake.blake2b;

import suit.algorithms.hash.blake.BlakeLongCommon;
import suit.algorithms.hash.blake.BlakeMessageHasher;
import suit.algorithms.hash.blake.BlakeMessageHasherConfig;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;
import suit.tools.arrays.factory.LongDataFactory;

import java.util.Arrays;

/**
 * Input manager for large Messages hashing with Blake2b
 */
public final class Blake2bMessageHasher extends BlakeMessageHasher {
    private final Blake2bInstance inst;                 //Underlying instance
    private final IDataArrayFactory backingFactory;     //Factory for specific DataArray implementation

    //Condstructor from Config
    public Blake2bMessageHasher(final BlakeMessageHasherConfig conf) {
        super(conf);
        DataArray salt = conf.getSalt();
        long[] lSalt = null;
        if(salt != null) lSalt = salt.asLongArray();
        backingFactory = new LongDataFactory(DataArray.Endianess.LITTLE);
        this.inst = new Blake2bInstance(conf.getOutLen(),lSalt);
    }


    //Condstructor
    protected Blake2bMessageHasher(byte outLen, long[] salt) {
        super(outLen);
        backingFactory = new LongDataFactory(DataArray.Endianess.LITTLE);
        this.inst =  new Blake2bInstance(outLen,salt); //do we need other endian?
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
    protected final void putBytes(final int i, final byte[] data, final int dataIndex) {
        inst.m[i >>> 3] = BlakeLongCommon.getLongLittle(data, dataIndex);
    }

    @Override
    protected final int getAlign() {
        return 8;
    }

    //Produce the Final value
    public final DataArray finalStep()
    {
        inst.countUp(bufIndex);
        int open = bufIndex%8;                              //whats Open
        if(open != 0){
            //byte padding
            Arrays.fill(notEven, open, 8, (byte) 0);        //pad
            putBytes(bufIndex,notEven,0);                   //put padded rest
            bufIndex += 8-open;
        }

        if(bufIndex != 0 && bufIndex != 128){
            //long Padding
            Arrays.fill(inst.m, bufIndex >>> 3, 16, 0L);
        }

        inst.flagFinal();      //Mark last compress
        inst.compress();       //Do Last Compress

        return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
    }

}
