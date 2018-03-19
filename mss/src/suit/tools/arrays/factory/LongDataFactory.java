package suit.tools.arrays.factory;


import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.LongDataArray;
import suit.tools.arrays.Transformers;

/**
 * IDataArrayFactory with Long as Primitive
 * Converting between different implementation is depending on endianess
 */
public class LongDataFactory implements IDataArrayFactory{

    //the endianess used in conversions
    DataArray.Endianess endian;

    public LongDataFactory(DataArray.Endianess endian) {
        this.endian = endian;
    }

    @Override
    public DataArray create() {
        return new LongDataArray(new long[0], endian);
    }

    @Override
    public DataArray create(byte value) {
        return new LongDataArray(new long[]{value}, endian);
    }

    @Override
    public DataArray create(short value) {
        return new LongDataArray(new long[]{value}, endian);
    }

    @Override
    public DataArray create(int value) {
        return new LongDataArray(new long[]{value}, endian);
    }

    @Override
    public DataArray create(long value) {
        return new LongDataArray(new long[]{value}, endian);
    }

    @Override
    public DataArray create(byte[] value) {
//        assert((value.length & 7) == 0);
        long[] res = new long[value.length >>> 3];
        Transformers.copyTo(value,res,0,res.length,endian);
        return create(res);
    }

    @Override
    public DataArray create(short[] value) {
        assert((value.length & 3) == 0);
        long[] res = new long[value.length >>> 2];
        Transformers.copyTo(value,res,0,res.length,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(int[] value) {
        assert((value.length & 1) == 0);
        long[] res = new long[value.length >>> 1];
        Transformers.copyTo(value,res,0,res.length,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(long[] value) {
        return new LongDataArray(value,endian);
    }

    @Override
    public DataArray create(DataHolder bs) {
        return create(bs.getData());
    }

    @Override
    public DataArray concat(DataArray... arrs) {
        int size = 0;
        for(DataArray arr:arrs) size += arr.getByteSize();
        assert((size & 7) == 0);
        long[] res = new long[size >>> 3];
        int i = 0;
        for(DataArray arr:arrs) {
            long[] arrL;
            if(arr instanceof LongDataArray)arrL = ((LongDataArray)arr).data;
            else arrL = arr.asLongArray();
            Transformers.copyTo(arrL,res,i,res.length,endian,arr.getEndianess() == DataArray.Endianess.LITTLE);
            i+= arrL.length;
        }
        return create(res);
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        assert(in.getByteSize() <= len && (len & 7) == 0);
        long[] res = new long[len>>>3];
        in.copyTo(res);
        return create(res);
    }
}
