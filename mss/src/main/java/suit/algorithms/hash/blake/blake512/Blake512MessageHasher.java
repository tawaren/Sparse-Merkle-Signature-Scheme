package suit.algorithms.hash.blake.blake512;

import suit.algorithms.hash.blake.BlakeLongCommon;
import suit.algorithms.hash.blake.BlakeMessageHasher;
import suit.algorithms.hash.blake.BlakeMessageHasherConfig;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;
import suit.tools.arrays.factory.IntDataFactory;

public final class Blake512MessageHasher extends BlakeMessageHasher {
    private Blake512Instance inst;
    private final IDataArrayFactory backingFactory;     //Factory for specific DataArray implementation


    public Blake512MessageHasher(final BlakeMessageHasherConfig conf) {
        super(conf);
        DataArray salt = conf.getSalt();
        long[] lSalt = null;
        if(salt != null) lSalt = salt.asLongArray();
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.inst = new Blake512Instance(lSalt);
    }

    //not used as FileHasher
    protected Blake512MessageHasher(byte outLen, long[] salt) {
        super(outLen);
        backingFactory = new IntDataFactory(DataArray.Endianess.LITTLE);
        this.inst =  new Blake512Instance(salt);
    }

    @Override
    protected void countUp(int n) {
        inst.countUp(n);
    }

    @Override
    protected void compress() {
        inst.compress();
    }


    @Override
    protected void putBytes(int i, byte[] data, int dataIndex) {
        inst.m[i >>> 3] = BlakeLongCommon.getLongBig(data, dataIndex);
    }

    @Override
    protected int getAlign() {
        return 8;
    }


    public final DataArray finalStep()
    {
        if(bufIndex == 128){
            inst.countUp(128);
            inst.compress();
            bufIndex = 0;
        } else {
            inst.countUp(bufIndex);
        }
        inst.prepareFinalPart(bufIndex,notEven, bufIndex %8);
        inst.compress();
        return BlakeLongCommon.genResult(inst.getHash(), outLen, backingFactory);
    }

}
